package com.sns.project.chat.dto.response;

import com.sns.project.domain.chat.ChatParticipant;
import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.chat.ChatRoomType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ChatRoomResponse {
    private Long id;
    private String name;
    private ChatRoomType type;
    private List<ParticipantResponse> participants;

    public ChatRoomResponse(ChatRoom chatRoom, List<ChatParticipant> participants) {
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
