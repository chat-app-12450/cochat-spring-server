package com.sns.project.handler.exceptionHandler.exception.notfound;

public class NotFoundCommentException extends RuntimeException {
    public NotFoundCommentException(Long commentId) {
        super("Comment with ID " + commentId + " not found");
    }
}
