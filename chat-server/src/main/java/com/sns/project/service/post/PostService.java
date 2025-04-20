package com.sns.project.service.post;

import com.sns.project.service.NotificationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.sns.project.repository.post.PostRepository;
import com.sns.project.controller.post.post.response.PostSummaryDto;
import com.sns.project.repository.post.PostImageInfoRepository;
import com.sns.project.domain.post.Post;
import com.sns.project.domain.post.PostImageInfo;
import com.sns.project.domain.user.User;

import org.springframework.web.multipart.MultipartFile;
import com.sns.project.service.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

  private static final int MAX_RETRIES = 2;
  
  private final GCSService gcsService;
  private final PostRepository postRepository;
  private final PostImageInfoRepository postImageInfoRepository;
  private final UserService userService;
  private final ExecutorService executorService;
  private final NotificationService notificationService;

  public Long createPost(String title, String content, 
           List<MultipartFile> images, Long userId) {
    User user = userService.getUserById(userId);
    Post post = createPostEntity(title, content, user);
    Post savedPost = postRepository.save(post);

    if (images != null && !images.isEmpty()) {
      images.forEach(image -> processSingleImageAsync(image, post));
    }


//    notificationService.sendNotification("새로운 게시물이 등록되었습니다", user.getId(), List.of(1L, 2L, 3L));
    return savedPost.getId();
  }

  private Post createPostEntity(String title, String content, User user) {
    return Post.builder()
            .title(title)
            .content(content)
            .user(user)
            .images(new ArrayList<>())
            .build();
  }


  private CompletableFuture<Void> processSingleImageAsync(MultipartFile image, Post post) {
    try {
      byte[] fileBytes = image.getBytes();
      String originalFilename = image.getOriginalFilename();
      return uploadImageWithRetry(fileBytes, originalFilename, post, 0);
    } catch (IOException e) {
      log.error("Failed to read image for post {}: {}", post.getId(), e.getMessage());
      return CompletableFuture.completedFuture(null);
    }
  }

  private CompletableFuture<Void> uploadImageWithRetry(byte[] fileBytes, String fileName, Post post, int attempt) {
    return CompletableFuture.supplyAsync(() -> gcsService.uploadFile(fileBytes, fileName), executorService)
        .thenAccept(imgUrl -> saveImageInfo(imgUrl, post))
        .exceptionallyCompose(throwable -> handleUploadFailure(fileBytes, fileName, post, attempt, throwable));
  }

  private void saveImageInfo(String imgUrl, Post post) {
    log.info("Uploaded image : {}", imgUrl);
    postImageInfoRepository.save(new PostImageInfo(imgUrl, post));
  }

  private CompletableFuture<Void> handleUploadFailure(byte[] fileBytes, String fileName, Post post, int attempt, Throwable throwable) {
    if (attempt < MAX_RETRIES) {
      log.warn("Retrying upload (attempt {}/{}) for post {}... Reason: {}", 
      attempt, MAX_RETRIES, post.getId(), throwable.getMessage());
        return uploadImageWithRetry(fileBytes, fileName, post, attempt + 1);
    } else {
      log.error("Failed to process images for post {}", post.getId(), throwable);
      return CompletableFuture.completedFuture(null);
    }
  }

public Post getPostById(Long postId) {
    Post post = postRepository.findById(postId)
    .orElseThrow(() -> new RuntimeException("Post not found"));
    return post;
}

public Page<PostSummaryDto> getPopularPostsFromFollowing(Long userId, int page, int size) {
    userService.isExistUser(userId);
    Pageable pageable = PageRequest.of(page, size);
    return postRepository.findFollowingPostsOrderedByPopularity(userId, pageable);
}

public Page<PostSummaryDto> getLatestPostsFromFollowing(Long userId, int page, int size) {
    userService.isExistUser(userId);
    Pageable pageable = PageRequest.of(page, size);
    return postRepository.findAllPostsByUser(userId, pageable);
}


}