package com.sns.project.handler.exceptionHandler.exception.badRequest;


public class RegisterFailedException extends RuntimeException {

  public RegisterFailedException(String message){
    super(message);
  }

}
