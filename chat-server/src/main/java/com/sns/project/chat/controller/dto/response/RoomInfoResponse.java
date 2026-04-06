package com.sns.project.chat.controller.dto.response;

import com.sns.project.core.domain.chat.ChatParticipant;
import com.sns.project.core.domain.chat.ChatMessage;
import com.sns.project.core.domain.chat.ChatRoom;
import com.sns.project.core.domain.chat.ChatRoomType;
import com.sns.project.core.domain.product.Product;
import com.sns.project.core.domain.product.ProductStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class RoomInfoResponse {
    private Long id;
    private String name;
    private String description;
    private ChatRoomType type;
    private boolean openChat;
    private boolean joined;
    private int participantCount;
    private Integer maxParticipants;
    private String locationLabel;
    private Double latitude;
    private Double longitude;
    private Double distanceMeters;
    private ProductResponse product;
    private ParticipantResponse counterpart;
    private LastMessageResponse lastMessage;
    private long unreadCount;
    private List<ParticipantResponse> participants;

    public RoomInfoResponse(ChatRoom chatRoom, List<ChatParticipant> participants, Long currentUserId, ChatMessage lastMessage) {
        this(chatRoom, participants, currentUserId, lastMessage, 0L, null);
    }

    public RoomInfoResponse(ChatRoom chatRoom, List<ChatParticipant> participants, Long currentUserId, ChatMessage lastMessage, long unreadCount) {
        this(chatRoom, participants, currentUserId, lastMessage, unreadCount, null);
    }

    public RoomInfoResponse(
        ChatRoom chatRoom,
        List<ChatParticipant> participants,
        Long currentUserId,
        ChatMessage lastMessage,
        long unreadCount,
        Double distanceMeters
    ) {
        this.id = chatRoom.getId();
        this.name = chatRoom.getName();
        this.description = chatRoom.getDescription();
        this.type = chatRoom.getChatRoomType();
        this.openChat = chatRoom.isOpenChat();
        this.locationLabel = chatRoom.getLocationLabel();
        this.latitude = chatRoom.getLatitude();
        this.longitude = chatRoom.getLongitude();
        this.distanceMeters = distanceMeters;
        this.product = chatRoom.getProduct() != null ? new ProductResponse(chatRoom.getProduct()) : null;
        this.participants = participants.stream()
            .map(ParticipantResponse::new)
            .collect(Collectors.toList());
        this.participantCount = this.participants.size();
        this.maxParticipants = chatRoom.getMaxParticipants();
        this.joined = this.participants.stream().anyMatch(participant -> Objects.equals(participant.getId(), currentUserId));
        this.counterpart = chatRoom.getChatRoomType() == ChatRoomType.PRIVATE
            ? participants.stream()
                .map(ChatParticipant::getUser)
                .filter(user -> !Objects.equals(user.getId(), currentUserId))
                .findFirst()
                .map(ParticipantResponse::new)
                .orElse(null)
            : null;
        this.lastMessage = lastMessage != null ? new LastMessageResponse(lastMessage) : null;
        this.unreadCount = unreadCount;
    }

    @Getter
    public static class ProductResponse {
        private Long id;
        private String title;
        private ProductStatus status;

        public ProductResponse(Product product) {
            this.id = product.getId();
            this.title = product.getTitle();
            this.status = product.getStatus();
        }
    }

    @Getter
    public static class ParticipantResponse {
        private Long id;
        private String userId;
        private String name;
        private String profileImageUrl;

        public ParticipantResponse(ChatParticipant chatParticipant) {
            this(chatParticipant.getUser());
        }

        public ParticipantResponse(com.sns.project.core.domain.user.User user) {
            this.id = user.getId();
            this.userId = user.getUserId();
            this.name = user.getName();
            this.profileImageUrl = user.getProfile_image_url();
        }
    }

    @Getter
    public static class LastMessageResponse {
        private Long messageId;
        private Long senderId;
        private String senderName;
        private String content;
        private LocalDateTime receivedAt;

        public LastMessageResponse(ChatMessage chatMessage) {
            this.messageId = chatMessage.getId();
            this.senderId = chatMessage.getSender().getId();
            this.senderName = chatMessage.getSender().getName();
            this.content = chatMessage.getMessage();
            this.receivedAt = chatMessage.getReceivedAt();
        }
    }
}
