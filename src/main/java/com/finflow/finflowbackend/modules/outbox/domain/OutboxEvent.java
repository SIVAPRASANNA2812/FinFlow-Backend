package com.finflow.finflowbackend.modules.outbox.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name="outbox_events")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @Column(length=64)
    private String id;
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;
    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant processedAt;
}
