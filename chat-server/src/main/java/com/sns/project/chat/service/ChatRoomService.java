package com.sns.project.chat.service;

import com.sns.project.config.constants.RedisKeys;
import com.sns.project.domain.chat.ChatParticipant;
import com.sns.project.repository.chat.ChatParticipantRepository;
import java.util.ArrayList;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sns.project.domain.chat.ChatRoom;
import com.sns.project.domain.chat.ChatRoomType;
import com.sns.project.repository.chat.ChatRoomRepository;
import com.sns.project.service.user.UserService;
import com.sns.project.domain.user.User;
import com.sns.project.chat.dto.response.ChatRoomResponse;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserService userService;
    private final ChatRedisService stringRedisService;
    @Transactional
    public ChatRoomResponse createRoom(String name, List<Long> participantIds, User creator) {
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


        String roomUsersKey = RedisKeys.Chat.CHAT_ROOM_PARTICIPANTS_SET_KEY.getParticipants(chatRoom.getId());
        Set<Long> uniqueParticipantIds = new HashSet<>(participantIds);
        uniqueParticipantIds.add(creator.getId());
        List<User> participants = userService.getUsersByIds(uniqueParticipantIds);
        List<ChatParticipant> chatParticipants = new ArrayList<>();
        for (User participant : participants) {
            // 채팅방 참여자 목록 데이터베이스 저장
            ChatParticipant chatParticipant = new ChatParticipant(chatRoom, participant);
            chatParticipants.add(chatParticipantRepository.save(chatParticipant));
            // 채팅방 참여자 목록 캐시 업데이트
            stringRedisService.addToSet(roomUsersKey, participant.getId().toString());
        }

        
        return new ChatRoomResponse(chatRoom, chatParticipants);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getUserChatRooms(User user) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithParticipantsByUserId(user.getId());

        return chatRooms.stream()
            .map(chatRoom -> new ChatRoomResponse(
                chatRoom,
                chatRoom.getParticipants()))
            .collect(Collectors.toList());
    }

    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }


}
