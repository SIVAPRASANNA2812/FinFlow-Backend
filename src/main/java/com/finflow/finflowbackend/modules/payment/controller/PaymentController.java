package com.finflow.finflowbackend.modules.payment.controller;

import com.finflow.finflowbackend.common.exception.AccountNotFoundException;
import com.finflow.finflowbackend.modules.account.domain.Account;
import com.finflow.finflowbackend.modules.account.repository.AccountRepository;
import com.finflow.finflowbackend.modules.payment.domain.JournalEntry;
import com.finflow.finflowbackend.modules.payment.domain.Transaction;
import com.finflow.finflowbackend.modules.payment.dto.TransferRequest;
import com.finflow.finflowbackend.modules.payment.dto.TransferResponse;
import com.finflow.finflowbackend.modules.payment.repository.JournalEntryRepository;
import com.finflow.finflowbackend.modules.payment.repository.TransactionRepository;
import com.finflow.finflowbackend.modules.payment.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final TransferService transferService;
    private final TransactionRepository transactionRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> executeTransfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.transferFunds(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 1. Get all recent transactions
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAllByOrderByCreatedAtDesc());
    }

    // 2. Get double-entry journal entries for a specific account
    @GetMapping("/journal-entries/{accountNumber}")
    public ResponseEntity<List<JournalEntry>> getAccountJournalEntries(@PathVariable String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        return ResponseEntity.ok(journalEntryRepository.findByAccountIdOrderByCreatedAtDesc(account.getId()));
    }
}