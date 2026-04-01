package com.sns.project.chat.service.event;

import com.sns.project.chat.service.ChatRealtimeStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatRealtimeProjectionEventHandler {

    private final ChatRealtimeStateService chatRealtimeStateService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessageCreated(ChatMessageCreatedEvent event) {
        chatRealtimeStateService.incrementUnreadCounts(event.roomId(), event.senderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatRoomRead(ChatRoomReadEvent event) {
        chatRealtimeStateService.clearUnreadCount(event.roomId(), event.userId());
    }
}
