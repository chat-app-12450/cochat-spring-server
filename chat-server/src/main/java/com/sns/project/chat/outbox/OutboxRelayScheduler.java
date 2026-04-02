package com.sns.project.chat.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxRelayService outboxRelayService;

    @Scheduled(fixedDelayString = "${app.outbox.relay.fixed-delay-ms:1000}")
    public void relayPendingEvents() {
        outboxRelayService.relayPendingEvents();
    }
}
