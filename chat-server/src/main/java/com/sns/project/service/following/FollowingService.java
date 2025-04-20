package com.sns.project.service.following;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.sns.project.domain.follow.Follow;
import com.sns.project.domain.user.User;
import com.sns.project.controller.follow.dto.FollowingsResponse;
import com.sns.project.controller.follow.dto.FollowUserResponse;
import com.sns.project.controller.follow.FollowersResponse;
import com.sns.project.service.user.UserService;
import com.sns.project.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowingService {

    private static final Logger logger = LoggerFactory.getLogger(FollowingService.class);

    private final UserService userService;
    private final FollowRepository followRepository;

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        User follower = userService.getUserById(followerId);
        User following = userService.getUserById(followingId);        

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            followRepository.deleteByFollowerAndFollowing(follower, following);
            logger.info("User {} unfollowed user {}", followerId, followingId);
        } else {
            throw new IllegalArgumentException(String.format("Follow relationship does not exist: User %d -> User %d", followerId, followingId));
        }
    }

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }
        User follower = userService.getUserById(followerId);
        User following = userService.getUserById(followingId);

        try {
            if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
                followRepository.save(new Follow(follower, following));
                logger.info("User {} followed user {}", followerId, followingId);
            } else {
                logger.warn("Attempt to follow an existing relationship: User {} -> User {}", followerId, followingId);
                throw new IllegalArgumentException("Follow relationship already exists");
            }
        } catch (DataIntegrityViolationException e) {
            logger.warn("Concurrent follow request detected for User {} -> User {}", followerId, followingId);
        }
    }

    public FollowingsResponse getAllFollowings(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new FollowingsResponse(
            followRepository.findAllByFollowerId(userId, pageable).stream()
                .map(Follow::getFollowing)
                .map(FollowUserResponse::new)
                .collect(Collectors.toList())
        );
    }

    public FollowersResponse getAllFollowers(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new FollowersResponse(
            followRepository.findAllByFollowingId(userId, pageable).stream()
                .map(Follow::getFollower)
                .map(FollowUserResponse::new)
                .collect(Collectors.toList())
        );
    }
}
