package com.sns.project.controller.post.post.response;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSummaryDto {
    private Long postId;
    private String title;
    private String content;
    private String userName;
    private Long likeCount;
    private Long commentCount;

    @QueryProjection
    public PostSummaryDto(Long postId, String title, String content, String userName, Long likeCount, Long commentCount) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.userName = userName;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}
