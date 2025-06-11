package com.sns.project.chat.service;

import com.sns.project.chat.controller.dto.response.ChatHistoryResponse;
import com.sns.project.chat.controller.dto.response.RoomInfoResponse;
import com.sns.project.config.constants.RedisKeys;
import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.repository.chat.ChatMessageRepository;
import com.sns.project.core.repository.chat.ChatParticipantRepository;
import java.util.ArrayList;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.chat.ChatRoomType;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.service.user.UserService;
import com.sns.project.core.domain.user.User;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserService userService;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public RoomInfoResponse createRoom(String name, List<Long> participantIds, User creator) {
        if (participantIds.size() == 0) {
            throw new IllegalArgumentException("최소 두명의 참여자가 있어야합니다.");
        }
        ChatRoomType type = participantIds.size() > 2 ? ChatRoomType.GROUP : ChatRoomType.PRIVATE;
        
        ChatRoom chatRoom = ChatRoom.builder()
                                    .name(name)
                                    .chatRoomType(type)
                                    .creator(creator)
                                    .build();
        chatRoomRepository.save(chatRoom);


        Set<Long> uniqueParticipantIds = new HashSet<>(participantIds);
        uniqueParticipantIds.add(creator.getId());
        List<User> participants = userService.getUsersByIds(uniqueParticipantIds);
        List<ChatParticipant> chatParticipants = new ArrayList<>();
        for (User participant : participants) {
            // 채팅방 참여자 목록 데이터베이스 저장
            ChatParticipant chatParticipant = new ChatParticipant(chatRoom, participant);
            chatParticipants.add(chatParticipantRepository.save(chatParticipant));
        }

        
        return new RoomInfoResponse(chatRoom, chatParticipants);
    }

    @Transactional(readOnly = true)
    public List<RoomInfoResponse> getUserChatRooms(User user) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithParticipantsByUserId(user.getId());

        return chatRooms.stream()
            .map(chatRoom -> new RoomInfoResponse(
                chatRoom,
                chatRoom.getParticipants()))
            .collect(Collectors.toList());
    }

    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }

    public List<ChatHistoryResponse> getChatHistory(Long roomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(roomId);
        return chatMessages.stream()
            .map(chatMessage -> new ChatHistoryResponse(chatMessage.getId(), chatMessage.getMessage(), chatMessage.getSender().getId(), chatMessage.getReceivedAt()))
            .collect(Collectors.toList());
    }


}
