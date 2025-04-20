package com.sns.project.controller.post;

import com.sns.project.aspect.AuthRequired;
import com.sns.project.aspect.UserContext;
import com.sns.project.controller.post.post.response.PostsResponse;
import com.sns.project.controller.post.post.response.ResponsePostDto;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.controller.post.post.response.PostSummaryDto;
import com.sns.project.service.post.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "게시물 API", description = "게시물 CRUD API")
@SecurityRequirement(name = "Bearer Authentication")
public class PostController {

  private final PostService postService;




  @Operation(summary = "게시물 생성", description = "새로운 게시물을 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @AuthRequired
  @SecurityRequirement(name = "Bearer Authentication")
  public ApiResult<Long> createPost(
          @RequestPart("title") @NotNull(message = "제목은 필수입니다") String title,
          @RequestPart("content") @NotNull(message = "내용은 필수입니다") String content,
          @RequestPart(value = "images", required = false) List<MultipartFile> images) {

    Long userId = UserContext.getUserId();
    Long postId = postService.createPost(title, content, images, userId);
    return ApiResult.success(postId);
  }

  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류"),
      @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @DeleteMapping("/{postId}")
  @AuthRequired
  @SecurityRequirement(name = "Bearer Authentication")
  public ApiResult<String> deletePost(
          @PathVariable @Parameter(description = "게시물 ID") Long postId) {
    // postService.deletePost(postId);
    return ApiResult.success("게시물이 성공적으로 삭제되었습니다.");
  }

  @Operation(summary = "게시물 조회", description = "특정 게시물을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 조회 성공", 
          content = @Content(mediaType = "application/json", 
          schema = @Schema(implementation = ResponsePostDto.class))),
      @ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류"),
  })
  @AuthRequired
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping("/{postId}")
  public ApiResult<ResponsePostDto> getPost(@PathVariable @Parameter(description = "게시물 ID") Long postId) {
    ResponsePostDto res = new ResponsePostDto(postService.getPostById(postId));
    return ApiResult.success(res);
  }

  @Operation(summary = "인기순 게시물 조회", description = "팔로잉하는 사람들의 게시물을 인기순으로 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 조회 성공", 
          content = @Content(mediaType = "application/json", 
          array = @ArraySchema(schema = @Schema(implementation = ResponsePostDto.class)))),
      @ApiResponse(responseCode = "500", description = "서버 오류"),
  })
  @AuthRequired
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping("/following/popular")
  public ApiResult<PostsResponse> getPopularPostsFromFollowing(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    Long userId = UserContext.getUserId();
    Page<PostSummaryDto> res = postService.getPopularPostsFromFollowing(userId, page, size);
    return ApiResult.success(new PostsResponse(res.getContent(), res.getTotalPages(), res.getTotalElements()));
  }

  @Operation(summary = "최신순 게시물 조회", description = "팔로잉하는 사람들의 게시물을 최신순으로 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "게시물 조회 성공", 
          content = @Content(mediaType = "application/json", 
          array = @ArraySchema(schema = @Schema(implementation = ResponsePostDto.class)))),
      @ApiResponse(responseCode = "500", description = "서버 오류"),
  })
  @AuthRequired
  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping("/following/latest")
  public ApiResult<PostsResponse> getLatestPostsFromFollowing(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    Long userId = UserContext.getUserId();
    Page<PostSummaryDto> posts = postService.getLatestPostsFromFollowing(userId, page, size);
    return ApiResult.success(new PostsResponse(posts.getContent(), posts.getTotalPages(), posts.getTotalElements()));
  }

}
