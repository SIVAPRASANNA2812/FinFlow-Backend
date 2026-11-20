package com.finflow.finflowbackend.modules.audit.consumer;

import tools.jackson.databind.ObjectMapper;
import com.finflow.finflowbackend.common.config.KafkaTopicConfig;
import com.finflow.finflowbackend.modules.audit.domain.AuditLog;
import com.finflow.finflowbackend.modules.audit.repository.AuditLogRepository;
import com.finflow.finflowbackend.modules.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogConsumer {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    // Listens continuously to the "payment-events" topic!
    @KafkaListener(
            topics = KafkaTopicConfig.PAYMENT_EVENTS_TOPIC,
            groupId = "finflow-audit-group"
    )
    public void consumePaymentEvent(String message) {
        log.info(">>> [Kafka Consumer] Received payment event from Kafka: {}", message);

        try {
            // 1. Deserialize JSON string back to Java object
            PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);

            // 2. Map into MongoDB AuditLog Document
            AuditLog auditLog = AuditLog.builder()
                    .transactionId(event.getTransactionId())
                    .referenceNumber(event.getReferenceNumber())
                    .sourceAccountNumber(event.getSourceAccountNumber())
                    .destinationAccountNumber(event.getDestinationAccountNumber())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .action("FUNDS_TRANSFERRED")
                    .eventTimestamp(event.getTimestamp())
                    .ingestedAt(Instant.now())
                    .build();

            // 3. Save into MongoDB!
            auditLogRepository.save(auditLog);

            log.info(">>> [MongoDB] Successfully saved audit record for reference: {}", event.getReferenceNumber());

        } catch (Exception e) {
            log.error("Failed to process event in Kafka consumer: {}", e.getMessage(), e);
        }
    }
}