package com.sns.project.domain.chat;

import java.time.LocalDateTime;

import com.sns.project.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatReadStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private Long lastReadMessageId;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ChatReadStatus(User user, ChatRoom chatRoom, Long lastReadMessageId) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.lastReadMessageId = lastReadMessageId;
        this.updatedAt = LocalDateTime.now();
    }
    
}
