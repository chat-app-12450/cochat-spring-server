package com.sns.project.chat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sns.project.chat.outbox.ChatOutboxService;
import com.sns.project.core.domain.chat.ChatReadStatus;
import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.chat.ChatRoomType;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.repository.chat.ChatMessageRepository;
import com.sns.project.core.repository.chat.ChatParticipantRepository;
import com.sns.project.core.repository.chat.ChatReadStatusRepository;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.core.repository.product.ProductRepository;
import com.sns.project.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private UserService userService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatReadStatusRepository chatReadStatusRepository;

    @Mock
    private ChatRealtimeStateService chatRealtimeStateService;

    @Mock
    private ChatOutboxService chatOutboxService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    void markRoomAsRead_createsReadStatusWhenMissing() {
        Long roomId = 1L;
        Long userId = 2L;
        Long readUptoSeq = 10L;
        User user = createUser(userId);
        ChatRoom chatRoom = createRoom(roomId, user, 20L);
        ChatReadStatus savedStatus = new ChatReadStatus(user, chatRoom, readUptoSeq);

        when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatReadStatusRepository.findByUserIdAndRoomId(userId, roomId)).thenReturn(Optional.empty());
        when(userService.getUserById(userId)).thenReturn(user);
        when(chatReadStatusRepository.save(any(ChatReadStatus.class))).thenReturn(savedStatus);

        chatRoomService.markRoomAsRead(roomId, userId, readUptoSeq);

        verify(chatReadStatusRepository).save(any(ChatReadStatus.class));
        verify(chatReadStatusRepository, never()).updateIfLastReadSeqIsSmaller(any(), any(), any());
        verify(chatOutboxService).enqueueChatRoomRead(roomId, userId, null, readUptoSeq);
    }

    @Test
    void markRoomAsRead_keepsExistingReadSeqWhenAlreadyAhead() {
        Long roomId = 1L;
        Long userId = 2L;
        Long readUptoSeq = 10L;
        Long previousReadSeq = 15L;
        User user = createUser(userId);
        ChatRoom chatRoom = createRoom(roomId, user, 20L);
        ChatReadStatus existingStatus = new ChatReadStatus(user, chatRoom, previousReadSeq);

        when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatReadStatusRepository.findByUserIdAndRoomId(userId, roomId)).thenReturn(Optional.of(existingStatus));

        chatRoomService.markRoomAsRead(roomId, userId, readUptoSeq);

        verify(chatReadStatusRepository, never()).save(any(ChatReadStatus.class));
        verify(chatReadStatusRepository, never()).updateIfLastReadSeqIsSmaller(any(), any(), any());
        verify(chatOutboxService).enqueueChatRoomRead(roomId, userId, previousReadSeq, previousReadSeq);
    }

    @Test
    void markRoomAsRead_updatesExistingReadSeqWhenNewerSeqArrives() {
        Long roomId = 1L;
        Long userId = 2L;
        Long previousReadSeq = 10L;
        Long readUptoSeq = 20L;
        User user = createUser(userId);
        ChatRoom chatRoom = createRoom(roomId, user, 30L);
        ChatReadStatus existingStatus = new ChatReadStatus(user, chatRoom, previousReadSeq);

        when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatReadStatusRepository.findByUserIdAndRoomId(userId, roomId)).thenReturn(Optional.of(existingStatus));
        when(chatReadStatusRepository.updateIfLastReadSeqIsSmaller(userId, roomId, readUptoSeq)).thenReturn(1);

        chatRoomService.markRoomAsRead(roomId, userId, readUptoSeq);

        verify(chatReadStatusRepository).updateIfLastReadSeqIsSmaller(userId, roomId, readUptoSeq);
        verify(chatOutboxService).enqueueChatRoomRead(roomId, userId, previousReadSeq, readUptoSeq);
    }

    @Test
    void markRoomAsRead_recoversWhenInitialInsertRaces() {
        Long roomId = 1L;
        Long userId = 2L;
        Long readUptoSeq = 10L;
        Long reloadedReadSeq = 12L;
        User user = createUser(userId);
        ChatRoom chatRoom = createRoom(roomId, user, 20L);
        ChatReadStatus existingStatus = new ChatReadStatus(user, chatRoom, reloadedReadSeq);

        when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
        when(chatReadStatusRepository.findByUserIdAndRoomId(userId, roomId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(existingStatus));
        when(userService.getUserById(userId)).thenReturn(user);
        when(chatReadStatusRepository.save(any(ChatReadStatus.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate"));

        chatRoomService.markRoomAsRead(roomId, userId, readUptoSeq);

        verify(chatReadStatusRepository).save(any(ChatReadStatus.class));
        verify(chatReadStatusRepository, never()).updateIfLastReadSeqIsSmaller(any(), any(), any());
        verify(chatOutboxService).enqueueChatRoomRead(roomId, userId, reloadedReadSeq, reloadedReadSeq);
    }

    private User createUser(Long id) {
        return User.builder()
            .id(id)
            .email("user" + id + "@test.com")
            .password("pw")
            .name("user" + id)
            .userId("user" + id)
            .build();
    }

    private ChatRoom createRoom(Long roomId, User creator, Long lastMessageSeq) {
        return ChatRoom.builder()
            .id(roomId)
            .name("room")
            .chatRoomType(ChatRoomType.PRIVATE)
            .creator(creator)
            .lastMessageSeq(lastMessageSeq)
            .build();
    }
}
