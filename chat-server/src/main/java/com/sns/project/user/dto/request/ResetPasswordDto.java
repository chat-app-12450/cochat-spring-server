package com.sns.project.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPasswordDto {
    @NotBlank(message = "newPassword는 비어 있을 수 없습니다.")
    @Size(min = 8, max = 100, message = "newPassword는 8자 이상 100자 이하여야 합니다.")
    private String newPassword;
} 
