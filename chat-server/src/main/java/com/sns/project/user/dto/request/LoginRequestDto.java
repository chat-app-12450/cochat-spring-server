package com.sns.project.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "password는 비어 있을 수 없습니다.")
    @Size(max = 100, message = "password는 100자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "userId는 비어 있을 수 없습니다.")
    @Size(max = 30, message = "userId는 30자 이하여야 합니다.")
    private String userId;
}
