package com.finflow.finflowbackend.modules.audit.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id; // Generated automatically by MongoDB ObjectId

    private String transactionId;
    private String referenceNumber;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String action; // e.g. "FUNDS_TRANSFERRED"
    private Instant eventTimestamp;
    private Instant ingestedAt;
}