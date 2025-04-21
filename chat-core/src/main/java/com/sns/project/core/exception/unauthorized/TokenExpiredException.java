package com.sns.project.core.exception.unauthorized;

public class TokenExpiredException extends
    RuntimeException {

  TokenExpiredException(String token){
    super("token is expired");
  }
}
