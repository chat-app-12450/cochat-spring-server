package com.sns.project.handler.exceptionHandler.exception.unauthorized;

public class InvalidPasswordException extends
    RuntimeException {
  public InvalidPasswordException(){
    super("password is invalid");
  }
}
