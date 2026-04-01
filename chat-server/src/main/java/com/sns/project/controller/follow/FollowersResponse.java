package com.sns.project.controller.follow;

import com.sns.project.controller.follow.dto.FollowUserResponse;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowersResponse {

    private List<FollowUserResponse> followers;

    public FollowersResponse(List<FollowUserResponse> followers) {
        this.followers = followers;
    }
}
