package com.sns.project.core.repository.chat;

public interface RoomUnreadCountProjection {
    Long getRoomId();
    Long getUnreadCount();
}
