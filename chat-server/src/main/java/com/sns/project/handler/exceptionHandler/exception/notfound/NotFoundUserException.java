package com.sns.project.handler.exceptionHandler.exception.notfound;



public class NotFoundUserException extends RuntimeException{

    public NotFoundUserException(String userId){
      super("not existed user: "+userId);
    }
  }