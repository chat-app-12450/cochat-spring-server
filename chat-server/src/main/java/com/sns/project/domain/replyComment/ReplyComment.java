package com.sns.project.domain.replyComment;


import com.sns.project.domain.comment.Comment;
import com.sns.project.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "reply_comment")
@Getter
@NoArgsConstructor
public class ReplyComment {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String content;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "comment_id", nullable = false)
  private Comment comment;


  @OneToMany(mappedBy = "replyComment", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<ReplyCommentLike> replyCommentLikes = new ArrayList<>();

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }


  @Builder
  public ReplyComment(User user, Comment comment, String content) {
    this.user = user;
    this.comment = comment;
    this.content = content;
  }

}
