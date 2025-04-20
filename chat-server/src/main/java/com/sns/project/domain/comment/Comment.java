package com.sns.project.domain.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sns.project.domain.post.Post;
import com.sns.project.domain.user.User;
import com.sns.project.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = true)
  @JsonIgnore
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", nullable = true)
  private Comment parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Comment> replies = new ArrayList<>();

  @Column(nullable = false, length = 500)
  private String content;


  @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<CommentLike> commentLikes = new ArrayList<>();

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
      createdAt = LocalDateTime.now();
      updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
      updatedAt = LocalDateTime.now();
  }

public void makeDeleted() {
    this.content = "삭제된 댓글입니다.";
}
}

