package com.finflow.finflowbackend.modules.account.controller;

import com.finflow.finflowbackend.common.exception.AccountNotFoundException;
import com.finflow.finflowbackend.modules.account.domain.Account;
import com.finflow.finflowbackend.modules.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    // 1. Get all accounts (Used for dashboard balance cards & transfer dropdowns)
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    // 2. Get single account by account number
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
        return ResponseEntity.ok(account);
    }
}