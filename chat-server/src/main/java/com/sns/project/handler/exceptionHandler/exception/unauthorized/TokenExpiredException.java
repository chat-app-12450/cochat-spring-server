package com.sns.project.handler.exceptionHandler.exception.unauthorized;

public class TokenExpiredException extends
    RuntimeException {

  TokenExpiredException(String token){
    super("token is expired");
  }
}
