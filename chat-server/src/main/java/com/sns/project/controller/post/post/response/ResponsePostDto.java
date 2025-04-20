package com.sns.project.controller.post.post.response;

import java.util.List;
import java.util.stream.Collectors; 

import com.sns.project.domain.post.Post;
import com.sns.project.domain.post.PostImageInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "게시물 응답 DTO")
@Data
@AllArgsConstructor
public class ResponsePostDto {
    @Schema(description = "게시물 ID")
    private Long id;

    @Schema(description = "게시물 제목")
    private String title;

    @Schema(description = "게시물 내용")
    private String content;

    @Schema(description = "게시물 작성자 정보")
    private ResponseUserDto user;

    @Schema(description = "게시물 이미지 정보")
    private List<String> images;

    public ResponsePostDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.user = new ResponseUserDto(post.getUser());
        this.images = post.getImages().stream()
            .map(PostImageInfo::getImgUrl)
            .collect(Collectors.toList());
    }
}
