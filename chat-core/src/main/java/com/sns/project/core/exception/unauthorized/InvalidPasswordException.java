package com.sns.project.core.exception.unauthorized;

public class InvalidPasswordException extends
    RuntimeException {
  public InvalidPasswordException(){
    super("password is invalid");
  }
}
