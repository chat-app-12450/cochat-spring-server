package com.sns.project.user.dto.request;

import com.sns.project.core.domain.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
  @NotBlank(message = "email은 비어 있을 수 없습니다.")
  @Email(message = "email 형식이 올바르지 않습니다.")
  @Size(max = 255, message = "email은 255자 이하여야 합니다.")
  private String email;

  @NotBlank(message = "name은 비어 있을 수 없습니다.")
  @Size(max = 50, message = "name은 50자 이하여야 합니다.")
  private String name;

  @NotBlank(message = "password는 비어 있을 수 없습니다.")
  @Size(min = 8, max = 100, message = "password는 8자 이상 100자 이하여야 합니다.")
  private String password;

  @NotBlank(message = "userId는 비어 있을 수 없습니다.")
  @Size(min = 4, max = 30, message = "userId는 4자 이상 30자 이하여야 합니다.")
  @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "userId는 영문, 숫자, ., _, - 만 사용할 수 있습니다.")
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
