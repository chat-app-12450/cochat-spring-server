package com.sns.project.controller.comment;

import com.sns.project.aspect.AuthRequired;
import com.sns.project.aspect.UserContext;
import com.sns.project.controller.comment.dto.CommentListResponseDto;
import com.sns.project.controller.comment.dto.CommentReplyRequestDto;
import com.sns.project.controller.comment.dto.CommentRequestDto;
import com.sns.project.controller.comment.dto.CommentResponseDto;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import com.sns.project.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Comment", description = "APIs for managing comments")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Create a new comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @AuthRequired
    public ApiResult<CommentResponseDto> createComment(@RequestBody CommentRequestDto requestDto) {
        Long userId = UserContext.getUserId();
        CommentResponseDto responseDto = commentService.createComment(requestDto, userId);
        return ApiResult.success(responseDto);
    }

    @Operation(summary = "Create a reply to a comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reply created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{parentId}/replies")
    @AuthRequired
    public ApiResult<CommentResponseDto> createReply(@PathVariable Long parentId, @RequestBody CommentReplyRequestDto requestDto) {
        Long userId = UserContext.getUserId();
        CommentResponseDto responseDto = commentService.createReply(parentId, requestDto.getContent(), userId);
        return ApiResult.success(responseDto);
    }

    @Operation(summary = "Get comments by post ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/post/{postId}")
    @AuthRequired
    @SecurityRequirement(name = "Bearer Authentication")
    public ApiResult<CommentListResponseDto> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getComments(postId);
        return ApiResult.success(new CommentListResponseDto(comments));
    }

    @Operation(summary = "Get replies by comment ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Replies retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @GetMapping("/{commentId}/replies")
    @AuthRequired
    @SecurityRequirement(name = "Bearer Authentication")
    public ApiResult<CommentListResponseDto> getRepliesByComment(@PathVariable Long commentId) {
        List<CommentResponseDto> replies = commentService.getReplies(commentId);
        return ApiResult.success(new CommentListResponseDto(replies));
    }

    @Operation(summary = "Delete a comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{commentId}")
    @AuthRequired
    @SecurityRequirement(name = "Bearer Authentication")
    public ApiResult<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        commentService.deleteComment(commentId, userId);
        return ApiResult.success(null);
    }
}
