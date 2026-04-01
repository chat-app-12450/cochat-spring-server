package com.sns.project.chat.service;

import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.repository.chat.ChatMessageRepository;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.core.repository.user.UserRepository;
import com.sns.project.core.exception.notfound.ChatRoomNotFoundException;
import com.sns.project.core.exception.notfound.NotFoundUserException;
import com.sns.project.chat.service.event.ChatMessageCreatedEvent;

import org.springframework.context.ApplicationEventPublisher;
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
    private final ChatRoomService chatRoomService;
    private final ApplicationEventPublisher applicationEventPublisher;


    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException("User ID(" + userId + ") not found"));
    }




    /*
     * 메시지를 저장합니다.
     */
    @Transactional
    public ChatMessage saveMessage(Long roomId, Long senderId, String message) {
        // STOMP SEND 전에 interceptor에서도 확인하지만, 저장 직전 한 번 더 검증해서 우회를 막는다.
        chatRoomService.requireParticipant(roomId, senderId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        User sender = getUserById(senderId);

        ChatMessage savedMessage = chatMessageRepository.save(new ChatMessage(chatRoom, sender, message));
        chatRoom.updateLatestMessage(savedMessage);
        applicationEventPublisher.publishEvent(new ChatMessageCreatedEvent(roomId, senderId));
        
        return savedMessage;
    }



    



}
