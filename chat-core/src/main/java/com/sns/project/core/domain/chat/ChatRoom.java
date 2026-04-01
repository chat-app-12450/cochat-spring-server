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
        @Index(name = "idx_chat_room_latest_message_at", columnList = "latest_message_at")
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
    private ChatRoomType chatRoomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "latest_message_id")
    private Long latestMessageId;

    @Column(name = "latest_message_at")
    private LocalDateTime latestMessageAt;

    // @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    // private List<ChatMessage> messages = new ArrayList<>();

     @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
     @Builder.Default
     private List<ChatParticipant> participants = new ArrayList<>();

    public ChatRoom(String name, ChatRoomType chatRoomType, User creator, Product product) {
        this.name = name;
        this.chatRoomType = chatRoomType;
        this.creator = creator;
        this.product = product;
    }

    public void updateLatestMessage(ChatMessage chatMessage) {
        this.latestMessageId = chatMessage.getId();
        this.latestMessageAt = chatMessage.getReceivedAt();
    }
}
