package com.finflow.finflowbackend.modules.outbox.domain;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
