package com.sns.project.chat.service.event;

public record ChatMessageCreatedEvent(Long roomId, Long senderId) {
}
