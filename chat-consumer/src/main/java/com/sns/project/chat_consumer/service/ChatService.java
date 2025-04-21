package com.sns.project.chat_consumer.service;

import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.chat.ChatReadStatus;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.repository.chat.ChatMessageRepository;
import com.sns.project.core.repository.chat.ChatParticipantRepository;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.core.repository.chat.ChatReadStatusRepository;
import com.sns.project.core.repository.user.UserRepository;
import com.sns.project.core.exception.notfound.ChatRoomNotFoundException;
import com.sns.project.core.exception.duplication.DuplicatedMessageException;
import com.sns.project.core.exception.notfound.NotFoundUserException;
import com.sns.project.core.constants.RedisKeys;
import com.sns.project.chat_consumer.service.dto.LastReadIdInfo;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final RedisLuaService redisLuaService;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final ChatRedisService chatRedisService;

    /*
     * 유저의 읽음 처리 상태를 저장합니다.
     */
    @Transactional
    public void saveOrUpdateReadStatus(Long userId, Long roomId, Long messageId) {
        if(messageId == null){
            return;
        }
        User user = getUserById(userId);
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        
        chatReadStatusRepository.findByUserIdAndRoomId(userId, roomId)
            .ifPresentOrElse(
                (status) -> {
                    chatReadStatusRepository.updateIfLastReadIdIsSmaller(userId, roomId, messageId);
                },
                () -> {
                    ChatReadStatus newStatus = new ChatReadStatus(user, room, messageId);
                    chatReadStatusRepository.save(newStatus);
                }
            );
    
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException("User ID(" + userId + ") not found"));
    }

    public LastReadIdInfo readAllMessages(Long userId, Long roomId) {
        log.info("모든 메시지들 일음 처리~~~~~");
        String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(userId, roomId);
        String messageZSetKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();

        return redisLuaService.processUnreadMessages(lastReadKey, messageZSetKey, unreadCountKey);
    }



    /*
     * 메시지를 저장합니다.
     */
    @Transactional
    public Long saveMessage(Long roomId, Long senderId, String message, String clientMessageId) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        User sender = getUserById(senderId);
        
        chatMessageRepository.findByChatRoomAndClientMessageId(chatRoom, clientMessageId)
        .ifPresent(existing -> {
            throw new DuplicatedMessageException("중복 메시지입니다");
        });

        ChatMessage savedMessage = chatMessageRepository.save(new ChatMessage(chatRoom, sender, message, clientMessageId));
        
        return savedMessage.getId();
    }


    /*
     * 메시지별 안읽은 수를 조회합니다.
     */
    public int getUnreadCount(Long roomId, Long messageId) {
        String unreadCountKey = RedisKeys.Chat.CHAT_UNREAD_COUNT_HASH_KEY.getUnreadCountKey();
        return chatRedisService.getHashValue(unreadCountKey, messageId.toString())
            .map(Integer::parseInt)
            .orElse(calculateUnreadCount(roomId, messageId));
    }

    public int calculateUnreadCount(Long roomId, Long messageId) {
        String participantKey = RedisKeys.Chat.CHAT_ROOM_PARTICIPANTS_SET_KEY.getParticipants(roomId);
        Set<Long> participantIds = chatRedisService.getSetMembers(participantKey).stream()
            .map(Long::parseLong)
            .collect(Collectors.toSet());
    
        String messageZSetKey = RedisKeys.Chat.CHAT_MESSAGES_KEY.getMessagesKey(roomId);
        Long messageRank = chatRedisService.getRank(messageZSetKey, messageId.toString())
            .orElseThrow(() -> new RuntimeException("messageId " + messageId + "가 존재하지 않습니다"));
    
        int unreadCount = 0;
    
        for (Long participantId : participantIds) {
            String lastReadKey = RedisKeys.Chat.CHAT_LAST_READ_MESSAGE_ID.getLastReadMessageKey(participantId, roomId);
            Long lastReadMessageId = chatRedisService.getValue(lastReadKey)
                .map(Long::parseLong)
                .orElse(-1L); // 아무것도 안 읽었으면 -1로 간주
    
            Long lastReadRank = chatRedisService.getRank(messageZSetKey, lastReadMessageId.toString())
                .orElse(-1L); // 없으면 -1 (가장 오래된 위치)
    
            if (lastReadRank < messageRank) {
                unreadCount++;
            }
        }
    
        return unreadCount;
    }
    



}
