package com.sns.project.domain.follow;


import com.sns.project.domain.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "following_id"})
}) // 데이터 베이스에 동일한 팔로잉, 팔로워 관계 생기지 않도록 한다
public class Follow {


  public Follow(User follower, User following) {
    this.follower = follower;
    this.following = following;
  }


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private User following;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "following_id", nullable = false)
  private User follower;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();

  }
}
