package com.sns.project.chat.service.event;

public record ChatRoomReadEvent(Long roomId, Long userId) {
}
