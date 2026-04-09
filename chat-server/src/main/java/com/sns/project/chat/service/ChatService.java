package com.sns.project.chat.service;

import com.sns.project.chat.outbox.ChatOutboxService;
import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.repository.chat.ChatMessageRepository;
import com.sns.project.core.repository.chat.ChatParticipantRepository;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.core.repository.user.UserRepository;
import com.sns.project.core.exception.notfound.ChatRoomNotFoundException;
import com.sns.project.core.exception.notfound.NotFoundUserException;
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
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final ChatRoomService chatRoomService;
    private final ChatOutboxService chatOutboxService;
    private final ChatRealtimeStateService chatRealtimeStateService;


    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException("User ID(" + userId + ") not found"));
    }




    /*
     * 메시지를 저장합니다.
     */
    @Transactional
    public ChatMessage saveMessage(Long roomId, Long senderId, String message, String clientMessageId) {
        // STOMP SEND 전에 interceptor에서도 확인하지만, 저장 직전 한 번 더 검증해서 우회를 막는다.
        chatRoomService.requireParticipant(roomId, senderId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        User sender = getUserById(senderId);

        Long messageSeq = chatRoom.nextMessageSeq();
        long unreadCount = chatRealtimeStateService.countInitialMessageUnreadUsers(roomId, senderId);
        ChatMessage savedMessage = chatMessageRepository.save(
            new ChatMessage(chatRoom, sender, message, messageSeq, unreadCount)
        );
        chatRoom.updateLatestMessage(savedMessage);
        ChatParticipant senderParticipant = chatParticipantRepository
            .findTopByChatRoomIdAndUserIdAndLeaveSeqIsNullOrderByIdDesc(roomId, senderId)
            .orElseThrow(() -> new IllegalStateException("활성 참여자를 찾을 수 없습니다."));
        senderParticipant.markAsRead(messageSeq);
        // 메시지 저장과 원본 이벤트 적재를 같은 트랜잭션으로 묶는다.
        chatOutboxService.enqueueChatMessageCreated(savedMessage, clientMessageId, unreadCount);
        
        return savedMessage;
    }



    



}
