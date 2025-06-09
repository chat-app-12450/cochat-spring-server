package com.sns.project.chat.controller.dto.response;

import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.chat.ChatRoomType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class RoomInfoResponse {
    private Long id;
    private String name;
    private ChatRoomType type;
    private List<ParticipantResponse> participants;

    public RoomInfoResponse(ChatRoom chatRoom, List<ChatParticipant> participants) {
        this.id = chatRoom.getId();
        this.name = chatRoom.getName();
        this.type = chatRoom.getChatRoomType();
        this.participants = participants.stream()
            .map(ParticipantResponse::new)
            .collect(Collectors.toList());
    }

    @Getter
    public static class ParticipantResponse {
        private Long id;
        private String name;

        public ParticipantResponse(ChatParticipant chatParticipant) {
            this.id = chatParticipant.getUser().getId();
            this.name = chatParticipant.getUser().getName();
        }
    }
}
