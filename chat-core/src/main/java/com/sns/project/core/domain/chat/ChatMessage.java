package com.sns.project.core.domain.chat;

import java.time.LocalDateTime;

import com.sns.project.core.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
  indexes = {
    @Index(name = "idx_chat_message_room_id_id", columnList = "chat_room_id, id"),
    @Index(name = "idx_chat_message_room_id_message_seq", columnList = "chat_room_id, message_seq")
  }
  // uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id"})
)
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // @Column(nullable = false, unique = true)
    // private String clientMessageId;
    
    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(name = "message_seq", nullable = false)
    private Long messageSeq;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime receivedAt; // 서버가 받은 시각
    

    public ChatMessage(ChatRoom chatRoom, User sender, String message, Long messageSeq) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.messageSeq = messageSeq;
        this.message = message;
        // this.clientMessageId = clientMessageId;
        this.receivedAt = LocalDateTime.now();
    }
} 
