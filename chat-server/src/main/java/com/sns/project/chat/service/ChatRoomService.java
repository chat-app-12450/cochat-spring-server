package com.sns.project.chat.service;

import com.sns.project.chat.controller.dto.response.ChatHistoryResponse;
import com.sns.project.chat.controller.dto.response.ChatHistoryPageResponse;
import com.sns.project.chat.controller.dto.response.RoomInfoResponse;
import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.domain.chat.ChatReadStatus;
import com.sns.project.core.domain.product.Product;
import com.sns.project.core.repository.chat.ChatMessageRepository;
import com.sns.project.core.repository.chat.ChatParticipantRepository;
import com.sns.project.core.repository.chat.ChatReadStatusRepository;
import com.sns.project.core.exception.forbidden.ForbiddenException;
import com.sns.project.core.exception.badRequest.RegisterFailedException;
import com.sns.project.core.exception.notfound.NotFoundProductException;
import com.sns.project.core.repository.product.ProductRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.chat.ChatRoomType;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.chat.service.event.ChatRoomReadEvent;
import com.sns.project.user.UserService;
import com.sns.project.core.domain.user.User;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserService userService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final ChatRealtimeStateService chatRealtimeStateService;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public RoomInfoResponse createRoom(String name, List<Long> participantIds, User creator) {
        return createRoom(name, participantIds, creator, null);
    }

    @Transactional
    public RoomInfoResponse createProductRoom(Long productId, Long buyerId) {
        Product product = productRepository.findByIdWithSeller(productId)
            .orElseThrow(() -> new NotFoundProductException(productId));

        Long sellerId = product.getSeller().getId();
        if (sellerId.equals(buyerId)) {
            throw new RegisterFailedException("본인 상품에는 채팅방을 만들 수 없습니다.");
        }

        return chatRoomRepository.findPrivateProductRoomByProductIdAndParticipantIds(productId, sellerId, buyerId)
            .map(chatRoom -> new RoomInfoResponse(chatRoom, chatRoom.getParticipants(), buyerId, null))
            .orElseGet(() -> {
                User buyer = userService.getUserById(buyerId);
                return createRoom(product.getTitle(), List.of(sellerId), buyer, product);
            });
    }

    @Transactional
    private RoomInfoResponse createRoom(String name, List<Long> participantIds, User creator, Product product) {
        if (participantIds.size() == 0) {
            throw new IllegalArgumentException("최소 두명의 참여자가 있어야합니다.");
        }
        ChatRoomType type = participantIds.size() > 2 ? ChatRoomType.GROUP : ChatRoomType.PRIVATE;
        
        ChatRoom chatRoom = ChatRoom.builder()
                                    .name(name)
                                    .chatRoomType(type)
                                    .creator(creator)
                                    .product(product)
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
        return new RoomInfoResponse(chatRoom, chatParticipants, creator.getId(), null);
    }

    @Transactional(readOnly = true)
    public List<RoomInfoResponse> getUserChatRooms(User user) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithParticipantsByUserId(user.getId());
        List<Long> roomIds = chatRooms.stream()
            .map(ChatRoom::getId)
            .toList();
        List<Long> latestMessageIds = chatRooms.stream()
            .map(ChatRoom::getLatestMessageId)
            .filter(java.util.Objects::nonNull)
            .toList();
        Map<Long, ChatMessage> lastMessageById = new HashMap<>();
        if (!latestMessageIds.isEmpty()) {
            chatMessageRepository.findAllWithSenderByIdIn(latestMessageIds)
                .forEach(message -> lastMessageById.put(message.getId(), message));
        }
        Map<Long, Long> unreadCountByRoomId = roomIds.isEmpty()
            ? Map.of()
            : chatRealtimeStateService.getUnreadCounts(user.getId(), roomIds);

        return chatRooms.stream()
            .map(chatRoom -> new RoomInfoResponse(
                chatRoom,
                chatRoom.getParticipants(),
                user.getId(),
                lastMessageById.get(chatRoom.getLatestMessageId()),
                unreadCountByRoomId.getOrDefault(chatRoom.getId(), 0L)))
            .collect(Collectors.toList());
    }

    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public void requireParticipant(Long roomId, Long userId) {
        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            throw new ForbiddenException("채팅방 접근 권한이 없습니다.");
        }
    }

    @Transactional
    public ChatHistoryPageResponse getChatHistory(Long roomId, Long userId, Long beforeMessageId, int size) {
        // 히스토리는 "로그인한 사용자"가 아니라 "해당 방 참가자"에게만 보여야 한다.
        requireParticipant(roomId, userId);
        ChatRoom chatRoom = getChatRoomById(roomId);

        // 커서가 없으면 최신 메시지부터, 커서가 있으면 그 id보다 더 오래된 메시지부터 읽는다.
        // size + 1개를 읽는 이유는 다음 페이지 존재 여부(hasMore)를 판별하기 위해서다.
        List<ChatMessage> chatMessages = beforeMessageId == null
            ? chatMessageRepository.findRecentMessagesWithSender(roomId, PageRequest.of(0, size + 1))
            : chatMessageRepository.findMessagesWithSenderBeforeId(roomId, beforeMessageId, PageRequest.of(0, size + 1));

        boolean hasMore = chatMessages.size() > size;
        if (hasMore) {
            // 마지막 1개는 hasMore 판단용이라 실제 응답에서는 제외한다.
            chatMessages = new ArrayList<>(chatMessages.subList(0, size));
        }

        // DB에서는 DESC로 잘라와야 "이전 페이지"를 정확히 가져오기 쉽다.
        // beforeMessageId - 30 같은 숫자 계산으로 범위를 자르면 id가 중간에 비어 있거나
        // 삭제된 메시지가 있는 경우 페이지 경계가 틀어질 수 있으므로, "id < beforeMessageId" 커서를 그대로 쓴다.
        // 화면은 오래된 메시지 -> 최신 메시지 순이 자연스러우므로 응답 직전에만 뒤집는다.
        Collections.reverse(chatMessages);

        List<ChatHistoryResponse> messages = chatMessages.stream()
            .map(chatMessage -> new ChatHistoryResponse(chatMessage.getId(), chatMessage.getMessage(), chatMessage.getSender().getId(), chatMessage.getReceivedAt()))
            .collect(Collectors.toList());

        // reverse 이후 첫 메시지가 "이번 페이지에서 가장 오래된 메시지"다.
        // 다음 요청은 이 id보다 더 작은 메시지들만 가져오면 되므로 before 커서로 내려준다.
        Long nextBeforeMessageId = hasMore && !messages.isEmpty() ? messages.get(0).getId() : null;
        markRoomAsRead(chatRoom, userId, chatRoom.getLatestMessageId());
        applicationEventPublisher.publishEvent(new ChatRoomReadEvent(roomId, userId));
        return new ChatHistoryPageResponse(messages, nextBeforeMessageId, hasMore);
    }

    @Transactional
    public void markRoomAsRead(Long roomId, Long userId, Long messageId) {
        requireParticipant(roomId, userId);
        ChatRoom chatRoom = getChatRoomById(roomId);
        markRoomAsRead(chatRoom, userId, messageId != null ? messageId : chatRoom.getLatestMessageId());
        applicationEventPublisher.publishEvent(new ChatRoomReadEvent(roomId, userId));
    }

    private void markRoomAsRead(ChatRoom chatRoom, Long userId, Long messageId) {
        if (messageId == null) {
            return;
        }

        // 1차 시도: 이미 읽음 상태 row가 있다면 "더 큰 messageId로만" 바로 갱신한다.
        // 늦게 도착한 예전 읽음 요청(예: 100)이 최신 읽음 포인터(예: 120)를 덮어쓰지 못하게
        // update 조건을 lastReadMessageId < newMessageId 로 제한해둔다.
        int updated = chatReadStatusRepository.updateIfLastReadIdIsSmaller(userId, chatRoom.getId(), messageId);
        if (updated > 0) {
            return;
        }

        // 2차 시도: update가 0건이었다는 건 두 경우다.
        // - 이미 더 큰 읽음 포인터가 저장돼 있어서 갱신할 필요가 없음
        // - 이 유저/방 조합의 읽음 상태 row가 아직 없음
        // row가 있으면 엔티티 메서드로 한 번 더 안전하게 갱신하고,
        // 없으면 처음 읽는 사용자이므로 새 ChatReadStatus를 만든다.
        chatReadStatusRepository.findByUserIdAndRoomId(userId, chatRoom.getId())
            .ifPresentOrElse(
                readStatus -> readStatus.markAsRead(messageId),
                () -> chatReadStatusRepository.save(
                    new ChatReadStatus(
                        userService.getUserById(userId),
                        chatRoom,
                        messageId))
            );
    }


}
