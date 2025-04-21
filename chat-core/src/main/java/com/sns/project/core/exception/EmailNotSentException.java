package com.sns.project.core.exception;

public class EmailNotSentException extends RuntimeException {

    public EmailNotSentException(){
        super("메일 전송에 실패했습니다");
    }
}
