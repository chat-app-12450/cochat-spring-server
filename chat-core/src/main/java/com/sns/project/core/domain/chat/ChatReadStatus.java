package com.sns.project.core.domain.chat;

import java.time.LocalDateTime;

import com.sns.project.core.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(name = "uk_chat_read_status_user_room", columnNames = {"user_id", "chat_room_id"}),
    indexes = {
        @Index(name = "idx_chat_read_status_user_room", columnList = "user_id, chat_room_id")
    }
)
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

    @Column(name = "last_read_seq", nullable = false)
    private Long lastReadSeq;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ChatReadStatus(User user, ChatRoom chatRoom, Long lastReadSeq) {
        this.user = user;
        this.chatRoom = chatRoom;
        this.lastReadSeq = lastReadSeq;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsRead(Long lastReadSeq) {
        if (this.lastReadSeq == null || this.lastReadSeq < lastReadSeq) {
            this.lastReadSeq = lastReadSeq;
            this.updatedAt = LocalDateTime.now();
        }
    }
}
