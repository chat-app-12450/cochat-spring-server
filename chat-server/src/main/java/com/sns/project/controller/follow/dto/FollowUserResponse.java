package com.sns.project.controller.follow.dto;

import com.sns.project.domain.user.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUserResponse {
    private Long id;
    private String name;
    public FollowUserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
}