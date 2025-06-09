package com.sns.project.chat.controller.dto.response;

import java.util.List;

import lombok.Getter;

@Getter
public class RoomListResponse {
    public RoomListResponse(List<RoomInfoResponse> userChatRooms) {
        this.chatRooms = userChatRooms;
    }

    private List<RoomInfoResponse> chatRooms;
}
