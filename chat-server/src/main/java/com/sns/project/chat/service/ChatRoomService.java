package com.sns.project.chat.service;

import com.sns.project.chat.outbox.ChatOutboxService;
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
import com.sns.project.core.repository.chat.MessageUnreadCountProjection;
import com.sns.project.core.exception.forbidden.ForbiddenException;
import com.sns.project.core.exception.badRequest.RegisterFailedException;
import com.sns.project.core.exception.notfound.NotFoundProductException;
import com.sns.project.core.repository.product.ProductRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.chat.ChatRoomType;
import com.sns.project.core.domain.user.User;
import com.sns.project.core.repository.chat.ChatRoomRepository;
import com.sns.project.user.UserService;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserService userService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final ChatRealtimeStateService chatRealtimeStateService;
    private final ChatOutboxService chatOutboxService;
    private final ProductRepository productRepository;

    @Transactional
    public RoomInfoResponse createRoom(String name, List<Long> participantIds, User creator) {
        return createRoom(name, participantIds, creator, null, null, false, null);
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
                return createRoom(product.getTitle(), List.of(sellerId), buyer, product, null, false, null);
            });
    }

    @Transactional
    public RoomInfoResponse createOpenGroupRoom(String name, String description, Integer maxParticipants, User creator) {
        ChatRoom chatRoom = ChatRoom.builder()
            .name(name.trim())
            .description(normalizeDescription(description))
            .chatRoomType(ChatRoomType.GROUP)
            .openChat(true)
            .maxParticipants(maxParticipants)
            .creator(creator)
            .build();
        chatRoomRepository.save(chatRoom);

        ChatParticipant creatorParticipant = chatParticipantRepository.save(new ChatParticipant(chatRoom, creator));
        return new RoomInfoResponse(chatRoom, List.of(creatorParticipant), creator.getId(), null);
    }

    @Transactional
    private RoomInfoResponse createRoom(String name, List<Long> participantIds, User creator, Product product, String description, boolean openChat, Integer maxParticipants) {
        if (participantIds.size() == 0) {
            throw new IllegalArgumentException("최소 두명의 참여자가 있어야합니다.");
        }
        Set<Long> uniqueParticipantIds = new HashSet<>(participantIds);
        uniqueParticipantIds.add(creator.getId());
        ChatRoomType type = uniqueParticipantIds.size() > 2 ? ChatRoomType.GROUP : ChatRoomType.PRIVATE;
        
        ChatRoom chatRoom = ChatRoom.builder()
                                    .name(name)
                                    .description(normalizeDescription(description))
                                    .chatRoomType(type)
                                    .openChat(openChat)
                                    .maxParticipants(maxParticipants)
                                    .creator(creator)
                                    .product(product)
                                    .build();
        chatRoomRepository.save(chatRoom);


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
    public List<RoomInfoResponse> searchOpenGroupRooms(String keyword, Long userId) {
        List<ChatRoom> groupRooms = chatRoomRepository.searchOpenGroupRooms(normalizeKeyword(keyword));
        Map<Long, ChatMessage> lastMessageById = loadLastMessages(groupRooms);

        return groupRooms.stream()
            .map(chatRoom -> new RoomInfoResponse(
                chatRoom,
                chatRoom.getParticipants(),
                userId,
                lastMessageById.get(chatRoom.getLatestMessageId())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoomInfoResponse> getJoinedOpenGroupRooms(Long userId) {
        List<ChatRoom> groupRooms = chatRoomRepository.findJoinedOpenChatRoomsByUserId(userId);
        Map<Long, ChatMessage> lastMessageById = loadLastMessages(groupRooms);

        return groupRooms.stream()
            .map(chatRoom -> new RoomInfoResponse(
                chatRoom,
                chatRoom.getParticipants(),
                userId,
                lastMessageById.get(chatRoom.getLatestMessageId())))
            .collect(Collectors.toList());
    }

    @Transactional
    public RoomInfoResponse joinOpenGroupRoom(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        validateOpenGroupRoom(chatRoom);

        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            if (chatRoom.getMaxParticipants() != null && chatRoom.getParticipants().size() >= chatRoom.getMaxParticipants()) {
                throw new RegisterFailedException("채팅방 정원이 가득 찼습니다.");
            }
            User user = userService.getUserById(userId);
            chatParticipantRepository.save(new ChatParticipant(chatRoom, user));
            chatRoom = chatRoomRepository.findByIdWithParticipants(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        }

        Map<Long, ChatMessage> lastMessageById = loadLastMessages(List.of(chatRoom));
        return new RoomInfoResponse(chatRoom, chatRoom.getParticipants(), userId, lastMessageById.get(chatRoom.getLatestMessageId()));
    }

    @Transactional
    public void leaveOpenGroupRoom(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        validateOpenGroupRoom(chatRoom);

        if (!chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            throw new ForbiddenException("채팅방 참여자가 아닙니다.");
        }

        chatParticipantRepository.deleteByChatRoomIdAndUserId(roomId, userId);
    }

    @Transactional(readOnly = true)
    public List<RoomInfoResponse> getUserChatRooms(User user) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithParticipantsByUserId(user.getId());
        return buildRoomResponses(chatRooms, user.getId(), true);
    }

    @Transactional(readOnly = true)
    public RoomInfoResponse getRoomInfo(Long roomId, Long userId) {
        requireParticipant(roomId, userId);
        ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        return buildRoomResponses(List.of(chatRoom), userId, true).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }

    private List<RoomInfoResponse> buildRoomResponses(List<ChatRoom> chatRooms, Long userId, boolean includeUnreadCount) {
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
        Map<Long, Long> unreadCountByRoomId = !includeUnreadCount || roomIds.isEmpty()
            ? Map.of()
            : chatRealtimeStateService.getUnreadCounts(userId, roomIds);

        return chatRooms.stream()
            .map(chatRoom -> new RoomInfoResponse(
                chatRoom,
                chatRoom.getParticipants(),
                userId,
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
    public ChatHistoryPageResponse getChatHistory(Long roomId, Long userId, Long beforeMessageSeq, int size) {
        // 방 참가자인지 확인
        requireParticipant(roomId, userId);
        ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // DB는 조건에 맞는 전체 집합을 먼저 정렬한 뒤 LIMIT를 적용한다.
        // 따라서 `message_seq < beforeMessageSeq` 조건에서 ASC + LIMIT를 쓰면
        // "커서 바로 앞 30개"가 아니라 "가장 오래된 30개"가 잘릴 수 있다.
        // 그래서 먼저 DESC로 커서 직전 구간을 자르고, 응답 직전에만 reverse 해서
        // 화면에는 오래된 순으로 보여준다.
        List<ChatMessage> chatMessages = beforeMessageSeq == null
            ? chatMessageRepository.findRecentMessagesWithSender(roomId, PageRequest.of(0, size + 1))
            : chatMessageRepository.findMessagesWithSenderBeforeMessageSeq(roomId, beforeMessageSeq, PageRequest.of(0, size + 1));

        boolean hasMore = chatMessages.size() > size;
        if (hasMore) {
            // 마지막 1개는 hasMore 판단용이라 실제 응답에서는 제외한다.
            chatMessages = new ArrayList<>(chatMessages.subList(0, size));
        }

        // repository 쿼리는 DESC(최신순)로 가져오므로, 화면 표시 전에 reverse 해서
        // 오래된 순 -> 최신 순으로 맞춘다.
        Collections.reverse(chatMessages);

        RoomReadUpdate readUpdate = markRoomAsRead(chatRoom, userId, chatRoom.getLastMessageSeq());
        chatOutboxService.enqueueChatRoomRead(
            roomId,
            userId,
            readUpdate.previousReadSeq(),
            readUpdate.newReadSeq()
        );

        Map<Long, Long> unreadCountByMessageId = loadUnreadCounts(chatMessages);
        List<ChatHistoryResponse> messages = chatMessages.stream()
            .map(chatMessage -> new ChatHistoryResponse(
                chatMessage.getId(),
                chatMessage.getMessageSeq(),
                chatMessage.getMessage(),
                chatMessage.getSender().getId(),
                chatMessage.getReceivedAt(),
                unreadCountByMessageId.getOrDefault(chatMessage.getId(), 0L)))
            .collect(Collectors.toList());

        // reverse 이후 첫 메시지가 "이번 페이지에서 가장 오래된 메시지"다.
        // 다음 요청은 이 seq보다 더 작은 메시지들만 가져오면 되므로 before 커서로 내려준다.
        Long nextBeforeMessageSeq = hasMore && !messages.isEmpty() ? messages.get(0).getMessageSeq() : null;
        return new ChatHistoryPageResponse(messages, nextBeforeMessageSeq, hasMore);
    }

    @Transactional
    public void markRoomAsRead(Long roomId, Long userId, Long readUptoSeq) {
        requireParticipant(roomId, userId);
        ChatRoom chatRoom = getChatRoomById(roomId);
        Long targetReadSeq = readUptoSeq != null ? readUptoSeq : chatRoom.getLastMessageSeq();
        RoomReadUpdate readUpdate = markRoomAsRead(chatRoom, userId, targetReadSeq);

        // 읽음 projection reset은 chat.room.read 토픽 consumer가 Redis에 반영한다.
        chatOutboxService.enqueueChatRoomRead(
            roomId,
            userId,
            readUpdate.previousReadSeq(),
            readUpdate.newReadSeq()
        );
    }

    private RoomReadUpdate markRoomAsRead(ChatRoom chatRoom, Long userId, Long readUptoSeq) {
        if (readUptoSeq == null) {
            return new RoomReadUpdate(null, null);
        }

        ChatReadStatus existingReadStatus = chatReadStatusRepository.findByUserIdAndRoomId(userId, chatRoom.getId()).orElse(null);
        Long previousReadSeq = existingReadStatus != null ? existingReadStatus.getLastReadSeq() : null;
        if (previousReadSeq != null && previousReadSeq >= readUptoSeq) {
            return new RoomReadUpdate(previousReadSeq, previousReadSeq);
        }

        // 1차 시도: 이미 읽음 상태 row가 있다면 "더 큰 readSeq로만" 바로 갱신한다.
        // 늦게 도착한 예전 읽음 요청(예: seq 100)이 최신 읽음 포인터(예: seq 120)를 덮어쓰지 못하게
        // update 조건을 lastReadSeq < newReadSeq 로 제한해둔다.
        int updated = chatReadStatusRepository.updateIfLastReadSeqIsSmaller(userId, chatRoom.getId(), readUptoSeq);
        if (updated > 0) {
            return new RoomReadUpdate(previousReadSeq, readUptoSeq);
        }

        // 2차 시도: update가 0건이었다는 건 두 경우다.
        // - 이미 더 큰 읽음 포인터가 저장돼 있어서 갱신할 필요가 없음
        // - 이 유저/방 조합의 읽음 상태 row가 아직 없음
        // row가 있으면 엔티티 메서드로 한 번 더 안전하게 갱신하고,
        // 없으면 처음 읽는 사용자이므로 새 ChatReadStatus를 만든다.
        if (existingReadStatus != null) {
            existingReadStatus.markAsRead(readUptoSeq);
            return new RoomReadUpdate(previousReadSeq, existingReadStatus.getLastReadSeq());
        }

        ChatReadStatus readStatus = chatReadStatusRepository.save(
            new ChatReadStatus(
                userService.getUserById(userId),
                chatRoom,
                readUptoSeq));
        return new RoomReadUpdate(previousReadSeq, readStatus.getLastReadSeq());
    }

    private Map<Long, Long> loadUnreadCounts(List<ChatMessage> chatMessages) {
        Map<Long, Long> unreadCountByMessageId = new HashMap<>();
        if (chatMessages.isEmpty()) {
            return unreadCountByMessageId;
        }

        List<Long> messageIds = chatMessages.stream()
            .map(ChatMessage::getId)
            .toList();

        List<MessageUnreadCountProjection> unreadCounts = chatMessageRepository.countUnreadParticipantsByMessageIds(messageIds);
        for (MessageUnreadCountProjection unreadCount : unreadCounts) {
            unreadCountByMessageId.put(unreadCount.getMessageId(), unreadCount.getUnreadCount());
        }
        return unreadCountByMessageId;
    }

    private Map<Long, ChatMessage> loadLastMessages(List<ChatRoom> chatRooms) {
        List<Long> latestMessageIds = chatRooms.stream()
            .map(ChatRoom::getLatestMessageId)
            .filter(java.util.Objects::nonNull)
            .toList();

        Map<Long, ChatMessage> lastMessageById = new HashMap<>();
        if (!latestMessageIds.isEmpty()) {
            chatMessageRepository.findAllWithSenderByIdIn(latestMessageIds)
                .forEach(message -> lastMessageById.put(message.getId(), message));
        }
        return lastMessageById;
    }

    private void validateOpenGroupRoom(ChatRoom chatRoom) {
        if (!chatRoom.isOpenChat() || chatRoom.getChatRoomType() != ChatRoomType.GROUP) {
            throw new ForbiddenException("공개 그룹 채팅방만 참여하거나 나갈 수 있습니다.");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record RoomReadUpdate(Long previousReadSeq, Long newReadSeq) {
    }

}
