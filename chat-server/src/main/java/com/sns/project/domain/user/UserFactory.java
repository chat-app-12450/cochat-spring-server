package com.sns.project.domain.user;


import com.sns.project.controller.user.dto.request.RequestRegisterDto;

public class UserFactory {

  public static User createUser(RequestRegisterDto dto) {
    return User.builder()
        .email(dto.getEmail())
        .userId(dto.getUserId())
        .password(dto.getPassword())
        .name(dto.getName())
        .build();
  }


}

