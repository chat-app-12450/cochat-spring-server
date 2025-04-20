package com.sns.project.chat.dto.response;

import java.util.List;

import lombok.Getter;

@Getter
public class AllChatRoomResponse {
    public AllChatRoomResponse(List<ChatRoomResponse> userChatRooms) {
        this.chatRooms = userChatRooms;
    }

    private List<ChatRoomResponse> chatRooms;
}
