package com.finflow.finflowbackend.modules.payment.service;

import com.finflow.finflowbackend.common.exception.AccountNotFoundException;
import com.finflow.finflowbackend.common.exception.DuplicateTransactionException;
import com.finflow.finflowbackend.modules.account.domain.Account;
import com.finflow.finflowbackend.modules.account.repository.AccountRepository;
import com.finflow.finflowbackend.modules.outbox.domain.OutboxEvent;
import com.finflow.finflowbackend.modules.outbox.domain.OutboxStatus;
import com.finflow.finflowbackend.modules.outbox.repository.OutboxEventRepository;
import com.finflow.finflowbackend.modules.payment.domain.*;
import com.finflow.finflowbackend.modules.payment.dto.TransferRequest;
import com.finflow.finflowbackend.modules.payment.dto.TransferResponse;
import com.finflow.finflowbackend.modules.payment.event.PaymentCompletedEvent;
import com.finflow.finflowbackend.modules.payment.repository.JournalEntryRepository;
import com.finflow.finflowbackend.modules.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final JournalEntryRepository  journalEntryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransferResponse transferFunds(TransferRequest request) {
        if (transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).isPresent()){
            throw new DuplicateTransactionException("Transaction with this idempotency key already exists");
        }

        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())){
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        String sourceNum =  request.getSourceAccountNumber();
        String destinationNum =  request.getDestinationAccountNumber();

        Account sourceAccount;
        Account destinationAccount;

        if ( sourceNum.compareTo(destinationNum) <= 0 ){
            sourceAccount = accountRepository.findByAccountNumberWithLock(sourceNum)
                    .orElseThrow(()-> new AccountNotFoundException("Source account number not found" + sourceNum));
            destinationAccount = accountRepository.findByAccountNumberWithLock(destinationNum)
                    .orElseThrow(()-> new AccountNotFoundException("Destination account number not found" + destinationNum));
        } else {
            destinationAccount = accountRepository.findByAccountNumberWithLock(destinationNum)
                    .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + destinationNum));

            sourceAccount = accountRepository.findByAccountNumberWithLock(sourceNum)
                    .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + sourceNum));
        }

        // Verify source account currency matches request currency
        if (!sourceAccount.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new IllegalArgumentException(
                    "Source account currency (" + sourceAccount.getCurrency() + ") does not match requested currency (" + request.getCurrency() + ")"
            );
        }

        // Verify destination account currency matches request currency
        if (!destinationAccount.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new IllegalArgumentException(
                    "Destination account currency (" + destinationAccount.getCurrency() + ") does not match requested currency (" + request.getCurrency() + ")"
            );
        }

        sourceAccount.debit(request.getAmount());
        destinationAccount.credit(request.getAmount());

        String txid = UUID.randomUUID().toString();
        String refNumber = "TXN-"+ UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaction transaction = Transaction.builder()
                .id(txid)
                .referenceNumber(refNumber)
                .idempotencyKey(request.getIdempotencyKey())
                .transactionType(TransactionType.TRANSFER)
                .transactionStatus(TransactionStatus.COMPLETED)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Entry 1: DEBIT line for Source Account
        JournalEntry debitEntry = JournalEntry.builder()
                .id(UUID.randomUUID().toString())
                .transaction(savedTransaction)
                .account(sourceAccount)
                .entryType(EntryType.DEBIT)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .balanceAfter(sourceAccount.getBalance()) // Snapshot after debit
                .build();
// Entry 2: CREDIT line for Destination Account
        JournalEntry creditEntry = JournalEntry.builder()
                .id(UUID.randomUUID().toString())
                .transaction(savedTransaction)
                .account(destinationAccount)
                .entryType(EntryType.CREDIT)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .balanceAfter(destinationAccount.getBalance()) // Snapshot after credit
                .build();
// Enterprise Optimization: Save both in a single batch operation
        journalEntryRepository.saveAll(List.of(debitEntry, creditEntry));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .transactionId(savedTransaction.getId())
                .referenceNumber(savedTransaction.getReferenceNumber())
                .sourceAccountNumber(sourceAccount.getAccountNumber())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .amount(savedTransaction.getAmount())
                .currency(savedTransaction.getCurrency())
                .timestamp(Instant.now())
                .build();

        String payloadJson;
         try{
            payloadJson = objectMapper.writeValueAsString(event);
         }catch (Exception e){
             throw new RuntimeException("Error serializing outbox event", e);
         }

        OutboxEvent outboxEvent = new OutboxEvent();
         outboxEvent.setId(UUID.randomUUID().toString());
         outboxEvent.setAggregateType("TRANSACTION");
         outboxEvent.setAggregateId(savedTransaction.getId());
         outboxEvent.setEventType("PAYMENT_COMPLETED");
         outboxEvent.setPayload(payloadJson);
         outboxEvent.setStatus(OutboxStatus.PENDING);

         outboxEventRepository.save(outboxEvent);

        return TransferResponse.builder()
                .transactionId(savedTransaction.getId())
                .referenceNumber(savedTransaction.getReferenceNumber())
                .status(savedTransaction.getTransactionStatus())
                .amount(savedTransaction.getAmount())
                .currency(savedTransaction.getCurrency())
                .sourceBalanceAfter(sourceAccount.getBalance())
                .destinationBalanceAfter(destinationAccount.getBalance())
                .timestamp(Instant.now())
                .build();
    }
}
