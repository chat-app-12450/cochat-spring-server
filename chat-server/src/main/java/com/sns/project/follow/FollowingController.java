package com.sns.project.follow;

import com.sns.project.auth.AuthRequired;
import com.sns.project.auth.UserContext;
import com.sns.project.common.api.ApiResult;
import com.sns.project.follow.dto.FollowersResponse;
import com.sns.project.follow.dto.FollowingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/following")
@Tag(name = "Following Management System")
public class FollowingController {

    private final FollowingService followingService;

    @AuthRequired
    @PostMapping("/follow/{followingUserId}")
    @Operation(summary = "Follow a user", description = "Provide an ID to follow a specific user")
    public ApiResult<Long> followUser(
            @Parameter(description = "ID of the user to follow", required = true)
            @PathVariable Long followingUserId) {
        Long userId = UserContext.getUserId();
        followingService.followUser(userId, followingUserId);
        return ApiResult.success(followingUserId);
    }

    @AuthRequired
    @DeleteMapping("/unfollow/{followingUserId}")
    @Operation(summary = "Unfollow a user", description = "Provide an ID to unfollow a specific user")
    public String unfollowUser(
            @Parameter(description = "ID of the user to unfollow", required = true)
            @PathVariable Long followingUserId) {
        Long userId = UserContext.getUserId();
        followingService.unfollowUser(userId, followingUserId);
        return "User unfollowed successfully";
    }

    @AuthRequired
    @GetMapping("/followings")
    @Operation(summary = "Get all followings", description = "Retrieve a list of all users the current user is following")
    public ApiResult<FollowingsResponse> getAllFollowings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        FollowingsResponse response = followingService.getAllFollowings(userId, page, size);
        return ApiResult.success(response);
    }

    @AuthRequired
    @GetMapping("/followers")
    @Operation(summary = "Get all followers", description = "Retrieve a list of all users following the current user")
    public ApiResult<FollowersResponse> getAllFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        FollowersResponse response = followingService.getAllFollowers(userId, page, size);
        return ApiResult.success(response);
    }
}
