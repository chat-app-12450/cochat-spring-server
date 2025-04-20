package com.sns.project.service.comment;

import com.sns.project.domain.comment.Comment;
import com.sns.project.domain.post.Post;
import com.sns.project.domain.user.User;
import com.sns.project.controller.comment.dto.CommentRequestDto;
import com.sns.project.controller.comment.dto.CommentResponseDto;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundCommentException;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.UnauthorizedException;
import com.sns.project.repository.CommentRepository;
import com.sns.project.repository.post.PostRepository;
import com.sns.project.repository.UserRepository;
import com.sns.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public CommentResponseDto createComment(CommentRequestDto requestDto, Long userId) {
        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        User user = userService.getUserById(userId);

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(requestDto.getContent())
                .parent(null)
                .build();

        commentRepository.save(comment);

        return new CommentResponseDto(comment);
    }

    private Comment findParentComment(Long parentId) {
        return commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundCommentException(parentId));
    }

    @Transactional
    public CommentResponseDto createReply(Long parentId, String content, Long userId) {
        Comment parentComment = findParentComment(parentId);
        Post post = parentComment.getPost();
        User user = userService.getUserById(userId);

        Comment reply = Comment.builder()
                .post(post)
                .user(user)
                .content(content)
                .parent(parentComment)
                .build();

        commentRepository.save(reply);

        return new CommentResponseDto(reply);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long postId) {
        List<Comment> comments = commentRepository.findCommentByPostId(postId);
        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<CommentResponseDto> getReplies(Long commentId) {
        findParentComment(commentId);

        return commentRepository.findReplyCommentByParentId(commentId).stream()
            .map(CommentResponseDto::new)
            .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundCommentException(commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }

        if (comment.getReplies().isEmpty()) {
            comment.makeDeleted();
        } else {
            commentRepository.delete(comment);
        }
    }
}