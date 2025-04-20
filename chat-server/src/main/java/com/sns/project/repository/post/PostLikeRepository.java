package com.sns.project.repository.post;

import com.sns.project.domain.post.Post;
import com.sns.project.domain.post.PostLike;
import com.sns.project.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserAndPost(User user, Post post);
} 