package com.sns.project.core.repository.chat;

public interface MessageUnreadCountProjection {

    Long getMessageId();

    Long getUnreadCount();
}
