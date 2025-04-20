package com.sns.project.controller.post.post.response;

import com.sns.project.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "유저 응답 DTO")
@Setter
@Getter
public class ResponseUserDto {
    @Schema(description = "유저 ID")
    private Long id;

    @Schema(description = "유저 이름")
    private String name;

    public ResponseUserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }

    // Getters and setters
}
