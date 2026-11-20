package com.finflow.finflowbackend.modules.payment.repository;

import com.finflow.finflowbackend.modules.payment.domain.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, String> {
    List<JournalEntry> findByAccountIdOrderByCreatedAtDesc(String accountId);
    List<JournalEntry> findByTransactionId(String transactionId);
}
