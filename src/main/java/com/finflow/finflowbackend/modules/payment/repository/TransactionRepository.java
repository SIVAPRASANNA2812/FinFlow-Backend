package com.finflow.finflowbackend.modules.payment.repository;

import com.finflow.finflowbackend.modules.payment.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,String> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
    List<Transaction> findAllByOrderByCreatedAtDesc();
}
