package com.finflow.finflowbackend.modules.payment.domain;

import com.finflow.finflowbackend.modules.account.domain.Account;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="journal_entries")
@Builder
public class JournalEntry {
    @Id
    @Column(length = 64)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name="transaction_id",nullable = false)
    private Transaction transaction;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;
    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "balance_after", precision = 18, scale = 4, nullable = false)
    private BigDecimal balanceAfter;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
