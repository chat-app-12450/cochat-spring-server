package com.sns.project.core.domain.user;

public class UserFactory {

  public static User createUser(String email, String userId, String password, String name) {
    return User.builder()
        .email(email)
        .userId(userId)
        .password(password)
        .name(name)
        .build();
  }

} 