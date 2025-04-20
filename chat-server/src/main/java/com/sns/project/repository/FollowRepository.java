package com.sns.project.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sns.project.domain.follow.Follow;
import com.sns.project.domain.user.User;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    // void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    // boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);


    // List<User> findAllByFollowerId(Long userId);

    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId ORDER BY f.createdAt DESC")
    Page<Follow> findAllByFollowerId(@Param("followerId") Long followerId, Pageable pageable);

    @Query("SELECT f FROM Follow f WHERE f.following.id = :followingId ORDER BY f.createdAt DESC")
    Page<Follow> findAllByFollowingId(@Param("followingId") Long followingId, Pageable pageable);

    boolean existsByFollowerAndFollowing(User follower, User following);

    void deleteByFollowerAndFollowing(User follower, User following);
} 