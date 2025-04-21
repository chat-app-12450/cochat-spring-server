package com.sns.project.core.exception.notfound;



public class NotFoundUserException extends RuntimeException{

    public NotFoundUserException(String userId){
      super("not existed user: "+userId);
    }
  }