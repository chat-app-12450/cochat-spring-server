package com.sns.project.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestPasswordResetDto {
    @NotBlank(message = "email은 비어 있을 수 없습니다.")
    @Email(message = "email 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "email은 255자 이하여야 합니다.")
    private String email;
} 
