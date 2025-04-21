package com.sns.project.core.exception.notfound;

public class NotFoundCommentException extends RuntimeException {
    public NotFoundCommentException(Long commentId) {
        super("Comment with ID " + commentId + " not found");
    }
}
