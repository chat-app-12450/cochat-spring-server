package com.sns.project.core.domain.chat;

import com.sns.project.core.domain.user.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    indexes = {
        @Index(name = "idx_chat_participant_room_user_leave_seq", columnList = "chat_room_id, user_id, leave_seq"),
        @Index(name = "idx_chat_participant_room_leave_seq", columnList = "chat_room_id, leave_seq")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "join_seq", nullable = false)
    private Long joinSeq;

    @Column(name = "leave_seq")
    private Long leaveSeq;

    @Column(name = "last_read_seq", nullable = false)
    private Long lastReadSeq;

    @Builder
    public ChatParticipant(ChatRoom chatRoom, User user, Long joinSeq, Long leaveSeq, Long lastReadSeq) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.joinSeq = joinSeq;
        this.leaveSeq = leaveSeq;
        this.lastReadSeq = lastReadSeq;
    }

    public boolean isActive() {
        return leaveSeq == null;
    }

    public void markAsRead(Long readSeq) {
        if (readSeq == null) {
            return;
        }
        if (this.lastReadSeq == null || this.lastReadSeq < readSeq) {
            this.lastReadSeq = readSeq;
        }
    }

    public void leave(Long leaveSeq) {
        this.leaveSeq = leaveSeq;
    }
}
