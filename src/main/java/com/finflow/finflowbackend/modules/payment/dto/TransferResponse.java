package com.finflow.finflowbackend.modules.payment.dto;

import com.finflow.finflowbackend.modules.payment.domain.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse {
    private String transactionId;
    private String referenceNumber;
    private TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private BigDecimal sourceBalanceAfter;
    private BigDecimal destinationBalanceAfter;
    private Instant timestamp;
}
