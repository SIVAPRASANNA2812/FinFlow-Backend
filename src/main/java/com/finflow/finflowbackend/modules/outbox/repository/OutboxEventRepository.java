package com.finflow.finflowbackend.modules.outbox.repository;

import com.finflow.finflowbackend.modules.outbox.domain.OutboxEvent;
import com.finflow.finflowbackend.modules.outbox.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop50ByStatusOrderByCreatedAtDesc(OutboxStatus status);
}
