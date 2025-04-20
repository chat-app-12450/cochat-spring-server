package com.sns.project.repository.post;

import com.sns.project.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository 
extends JpaRepository<Post, Long> , PostRepositoryCustom {
} 