package com.finflow.finflowbackend.modules.account.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    @Id
    @Column(length = 64)
    private String id;
    @Column(name="account_number",unique = true,nullable = false,length=32)
    private String accountNumber;
    @Column(name = "holder_name", nullable = false, length = 100)
    private String holderName;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(precision = 18, scale = 4, nullable = false)
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status ;
    @Version
    private Long version;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean canDebit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (this.status != AccountStatus.ACTIVE) {
            return false;
        }
        return this.balance != null && this.balance.compareTo(amount) >= 0;
    }

    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot credit a closed account");
        }
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalStateException("Insufficient funds or account inactive");
        }
        this.balance = this.balance.subtract(amount);
    }
}
