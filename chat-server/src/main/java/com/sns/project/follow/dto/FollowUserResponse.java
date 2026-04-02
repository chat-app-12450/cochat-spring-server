package com.sns.project.follow.dto;

import com.sns.project.core.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowUserResponse {

    private Long id;
    private String userId;
    private String name;
    private String profileImageUrl;

    public FollowUserResponse(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.name = user.getName();
        this.profileImageUrl = user.getProfile_image_url();
    }
}
