package com.sns.project.repository.post;

import com.sns.project.controller.post.post.response.PostSummaryDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<PostSummaryDto> findFollowingPostsOrderedByPopularity(Long userId, Pageable pageable);
    Page<PostSummaryDto> findAllPostsByUser(Long userId, Pageable pageable);
}

