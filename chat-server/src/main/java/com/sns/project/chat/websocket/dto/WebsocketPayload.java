package com.sns.project.chat.websocket.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MessageBroadcast.class, name = "MESSAGE"),
    @JsonSubTypes.Type(value = JoinBroadcast.class, name = "JOIN"),
})
public interface WebsocketPayload {
    String getType();
}
