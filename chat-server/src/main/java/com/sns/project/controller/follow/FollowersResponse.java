package com.sns.project.controller.follow;

import com.sns.project.controller.follow.dto.FollowUserResponse;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowersResponse {
    private List<FollowUserResponse> followers;
}