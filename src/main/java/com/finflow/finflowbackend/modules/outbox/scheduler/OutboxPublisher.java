package com.finflow.finflowbackend.modules.outbox.scheduler;

import com.finflow.finflowbackend.common.config.KafkaTopicConfig;
import com.finflow.finflowbackend.modules.outbox.domain.OutboxEvent;
import com.finflow.finflowbackend.modules.outbox.domain.OutboxStatus;
import com.finflow.finflowbackend.modules.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // Runs automatically every 2000 milliseconds (2 seconds)
    @Scheduled(fixedRate = 2000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtDesc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            try {
                // 1. Send to Kafka topic "payment-events" using the transactionId as the message key
                kafkaTemplate.send(
                        KafkaTopicConfig.PAYMENT_EVENTS_TOPIC,
                        event.getAggregateId(),
                        event.getPayload()
                );

                // 2. Mark event as PUBLISHED in MySQL
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);

                log.info(">>> Successfully published outbox event [{}] to Kafka topic '{}'",
                        event.getId(), KafkaTopicConfig.PAYMENT_EVENTS_TOPIC);

            } catch (Exception e) {
                log.error("Failed to publish outbox event [{}] to Kafka: {}", event.getId(), e.getMessage());
            }
        }
    }
}