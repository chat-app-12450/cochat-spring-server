package com.sns.project.core.repository.outbox;

import com.sns.project.core.domain.outbox.OutboxEvent;
import com.sns.project.core.domain.outbox.OutboxStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findAllByStatusOrderByIdAsc(OutboxStatus status, Pageable pageable);
}
