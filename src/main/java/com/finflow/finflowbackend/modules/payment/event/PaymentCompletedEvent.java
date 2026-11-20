package com.finflow.finflowbackend.modules.payment.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCompletedEvent {
    private String transactionId;
    private String referenceNumber;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private Instant timestamp;
}
