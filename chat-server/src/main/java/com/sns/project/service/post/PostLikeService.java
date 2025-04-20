package com.sns.project.service.post;

import com.sns.project.domain.post.Post;
import com.sns.project.domain.post.PostLike;
import com.sns.project.domain.user.User;
import com.sns.project.repository.post.PostLikeRepository;
import com.sns.project.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j // ✅ 로깅 추가
@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserService userService;
    private final PostService postService;

    @Transactional
    public void toggleLike(Long userId, Long postId) {
        User user = userService.getUserById(userId);
        Post post = postService.getPostById(postId);
        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            removeLike(existingLike.get());
        } else {
            addLike(user, post);
        }
    }

    private void removeLike(PostLike postLike) {
        postLikeRepository.delete(postLike);
        log.info("User {} unliked post {}", postLike.getUser().getId(), postLike.getPost().getId());
    }

    private void addLike(User user, Post post) {
        try {
            postLikeRepository.save(new PostLike(user, post));
            log.info("User {} liked post {}", user.getId(), post.getId());
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate like request ignored for user {} on post {}", user.getId(), post.getId());
        }
    }
}
