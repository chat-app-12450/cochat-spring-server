package com.sns.project.domain.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sns.project.domain.follow.Follow;
import com.sns.project.domain.notification.Notification;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;
import lombok.*;

import java.util.List;
import lombok.ToString.Exclude;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"followers", "followings"})
@EqualsAndHashCode
@Builder
public class User  implements Serializable {

  public User(Long id) {
    this.id = id;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  @Column(unique = true, nullable = false)
  @NotBlank(message = "이메일은 필수 입력값입니다")
  private String email; // 이메일을 아이디로 사용

  @JsonIgnore
  @Column(nullable = false)
  @NotBlank(message = "비밀번호는 필수 입력값입니다")
  private String password;

  @Column(nullable = false)
  @NotBlank(message = "이름은 필수 입력값입니다")
  private String name;

  private String profile_image_url;

  @Column(unique = true, nullable = false)
  @NotBlank(message = "사용자 ID는 필수 입력값입니다")
  private String userId;

  @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<Follow> followers;

  @OneToMany(mappedBy = "following", fetch = FetchType.LAZY)
  @JsonIgnore
  private Set<Follow> followings;


  @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<Notification> receivedNotifications;

  public void setPassword(String password) {
    this.password = password;
  }
}