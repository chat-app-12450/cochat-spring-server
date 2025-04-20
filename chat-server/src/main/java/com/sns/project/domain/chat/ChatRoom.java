package com.sns.project.domain.chat;

import jakarta.persistence.CascadeType;
import java.util.ArrayList;
import java.util.List;

import com.sns.project.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    // private List<ChatMessage> messages = new ArrayList<>();

     @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
     @Builder.Default
     private List<ChatParticipant> participants = new ArrayList<>();

    public ChatRoom(String name, ChatRoomType chatRoomType, User creator) {
        this.name = name;
        this.chatRoomType = chatRoomType;
        this.creator = creator;
    }
}
