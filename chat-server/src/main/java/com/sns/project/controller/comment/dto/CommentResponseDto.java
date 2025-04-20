package com.sns.project.controller.comment.dto;

import com.sns.project.domain.comment.Comment;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponseDto {
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        if(comment.getPost() != null) {
            this.postId = comment.getPost().getId();
        }
        if(comment.getParent() != null) {
            this.parentId = comment.getParent().getId();
        }
        this.userId = comment.getUser().getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
} 