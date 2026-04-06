package com.sns.project.core.domain.chat;

import jakarta.persistence.CascadeType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.sns.project.core.domain.product.Product;
import com.sns.project.core.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
    indexes = {
        @Index(name = "idx_chat_room_latest_message_at", columnList = "latest_message_at"),
        @Index(name = "idx_chat_room_type", columnList = "chat_room_type"),
        @Index(name = "idx_chat_room_open_chat", columnList = "open_chat"),
        @Index(name = "idx_chat_room_last_message_seq", columnList = "last_message_seq"),
        @Index(name = "idx_chat_room_lat_lng", columnList = "latitude, longitude")
    })
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_room_type")
    private ChatRoomType chatRoomType;

    @Column(length = 500)
    private String description;

    @Column(name = "open_chat", nullable = false)
    @Builder.Default
    private boolean openChat = false;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "location_label", length = 120)
    private String locationLabel;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // 마지막 보낸 메시지의 아이디
    @Column(name = "latest_message_id")
    private Long latestMessageId;

    // 마지막 보낸 메시지의 시각
    @Column(name = "latest_message_at")
    private LocalDateTime latestMessageAt;

    // 마지막 보낸 메시지의 시퀀스(방 내에서 순서)
    @Column(name = "last_message_seq", nullable = false)
    @Builder.Default
    private Long lastMessageSeq = 0L;

    // @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    // private List<ChatMessage> messages = new ArrayList<>();

     @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
     @Builder.Default
     private List<ChatParticipant> participants = new ArrayList<>();

    public ChatRoom(
        String name,
        ChatRoomType chatRoomType,
        String description,
        boolean openChat,
        Integer maxParticipants,
        String locationLabel,
        Double latitude,
        Double longitude,
        User creator,
        Product product
    ) {
        this.name = name;
        this.chatRoomType = chatRoomType;
        this.description = description;
        this.openChat = openChat;
        this.maxParticipants = maxParticipants;
        this.locationLabel = locationLabel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creator = creator;
        this.product = product;
        this.lastMessageSeq = 0L;
    }

    public void updateLatestMessage(ChatMessage chatMessage) {
        this.latestMessageId = chatMessage.getId();
        this.latestMessageAt = chatMessage.getReceivedAt();
    }

    public Long nextMessageSeq() {
        this.lastMessageSeq = (this.lastMessageSeq == null ? 0L : this.lastMessageSeq) + 1L;
        return this.lastMessageSeq;
    }
}
