package com.sns.project.controller.user.dto.response;

import com.sns.project.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ResponseUserDto {
    private Long id;
    private String name;
    private String email;


    public ResponseUserDto(User user) {
        this.email = user.getEmail();
        this.id = user.getId();
        this.name = user.getName();
    }
}
