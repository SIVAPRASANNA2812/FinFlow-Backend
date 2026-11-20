package com.finflow.finflowbackend.modules.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="transactions")
@Builder
public class Transaction {

    @Id
    @Column(length = 64)
    private String id;
    @Column(name="idempotency_key",unique = true,nullable = false,length=64)
    private String idempotencyKey;
    @Column(name="reference_number",unique = true,nullable = false,length=64)
    private String referenceNumber;
    @Enumerated(EnumType.STRING)
    @Column(name="transaction_type",nullable = false,length=20)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    @Column(name="transaction_status",nullable = false,length=20)
    private TransactionStatus transactionStatus;
    @Column(precision =  18, scale = 2,nullable = false)
    private BigDecimal amount;
    @Column(nullable = false,length=3)
    private String currency;
    @Column(name = "failure_reason",length=255)
    private String failureReason;
    @CreationTimestamp
    @Column(name = "created_at",updatable = false)
    private Instant createdAt;

}
