package com.sns.project.handler.exceptionHandler.exception.notfound;

public class NotFoundEmailException extends RuntimeException{

  public NotFoundEmailException(String email){
    super("not existed email: "+email);
  }
}
