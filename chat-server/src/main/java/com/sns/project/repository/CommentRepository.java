package com.sns.project.repository;

import com.sns.project.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 게시글 댓글 조회
    @Query("SELECT c FROM Comment c WHERE c.parent.id IS NULL and c.post.id = :postId ORDER BY c.createdAt DESC")
    List<Comment> findCommentByPostId(Long postId);

    // 대댓글 조회
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId ORDER BY c.createdAt DESC")
    List<Comment> findReplyCommentByParentId(Long parentId);

} 