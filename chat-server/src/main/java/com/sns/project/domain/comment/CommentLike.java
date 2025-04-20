package com.sns.project.domain.comment;

import com.sns.project.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "comment_like", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "comment_id"})
}) // 한 사용자가 같은 댓글에 여러번 좋아요 누리지 못하게
@Entity
@Getter
@NoArgsConstructor
public class CommentLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "comment_id")
  private Comment comment;

  @Builder
  public CommentLike(User user, Comment comment) {
    this.user = user;
    this.comment = comment;
  }
}
