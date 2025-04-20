package com.sns.project.controller.post.post.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostsResponse {
    public PostsResponse(List<PostSummaryDto> posts, int totalPages, long totalElements) {
        this.posts = posts;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
    private List<PostSummaryDto> posts;
    private int totalPages;
    private long totalElements;
}
