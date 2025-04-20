package com.sns.project.handler.exceptionHandler.exception;

public class EmailNotSentException extends RuntimeException {

    public EmailNotSentException(){
        super("메일 전송에 실패했습니다");
    }
}
