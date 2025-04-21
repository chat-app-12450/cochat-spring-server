package com.sns.project.core.exception.badRequest;


public class RegisterFailedException extends RuntimeException {

  public RegisterFailedException(String message){
    super(message);
  }

}
