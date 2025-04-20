package com.sns.project.controller.user.dto.request;



import com.sns.project.domain.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestRegisterDto {
  private String email;
  private String name;
  private String password;
  private String userId;

  public static RequestRegisterDto fromEntity(User user) {
    return RequestRegisterDto.builder()
      .email(user.getEmail())
      .name(user.getName())
      .password(user.getPassword())
      .userId(user.getUserId())
      .build();
  }
}
