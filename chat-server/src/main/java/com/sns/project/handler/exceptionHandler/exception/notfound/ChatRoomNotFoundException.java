package com.sns.project.handler.exceptionHandler.exception.notfound;

public class ChatRoomNotFoundException extends RuntimeException {
    public ChatRoomNotFoundException(Long roomId) {
        super("채팅방을 찾을 수 없습니다. 채팅방 아이디: " + roomId);
    }
}
