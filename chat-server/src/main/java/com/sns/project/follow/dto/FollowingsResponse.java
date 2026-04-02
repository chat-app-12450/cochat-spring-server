package com.sns.project.follow.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowingsResponse {

    private List<FollowUserResponse> followings;

    public FollowingsResponse(List<FollowUserResponse> followings) {
        this.followings = followings;
    }
}
