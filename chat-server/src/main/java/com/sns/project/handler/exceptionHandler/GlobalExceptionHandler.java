package com.sns.project.handler.exceptionHandler;

import com.sns.project.handler.exceptionHandler.exception.badRequest.InvalidFormatRequestException;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundUserException;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.InvalidPasswordException;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.InvalidEmailTokenException;
import com.sns.project.handler.exceptionHandler.exception.notfound.ChatRoomNotFoundException;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundCommentException;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundEmailException;
import com.sns.project.handler.exceptionHandler.exception.notfound.NotFoundNotificationException;
import com.sns.project.handler.exceptionHandler.exception.badRequest.RegisterFailedException;
import com.sns.project.handler.exceptionHandler.exception.duplication.DuplicatedMessageException;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.TokenExpiredException;
import com.sns.project.handler.exceptionHandler.exception.unauthorized.UnauthorizedException;
import com.sns.project.handler.exceptionHandler.response.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ApiResult<?>> newResponse(Throwable throwable, HttpStatus httpStatus) {
    logger.error("Exception occurred - Type: {}, Message: {}, Status: {}", 
        throwable.getClass().getSimpleName(), 
        throwable.getMessage(), 
        httpStatus);

//    throwable.printStackTrace();

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity<>(ApiResult.error(throwable, httpStatus), headers, httpStatus);
  }

  /*
   * HttpStatus.CONFLICT (409)  
   * 중복된 메시지
   */
  @ExceptionHandler({
    DuplicatedMessageException.class
  })
  public ResponseEntity<?> handleDuplicatedMessage(RuntimeException ex) {
    return newResponse(ex, HttpStatus.CONFLICT);
  }

  /*
   * HttpStatus.BAD_REQUEST (400)
   * 잘못된 요청
   */
  @ExceptionHandler({
      RegisterFailedException.class, InvalidFormatRequestException.class
  })
  public ResponseEntity<?> handleBadRequest(RuntimeException ex) {
    return newResponse(ex, HttpStatus.BAD_REQUEST);
  }


  /*
   * HttpStatus.UNAUTHORIZED (401)
   * 인증 실패
   */
  @ExceptionHandler({
      InvalidPasswordException.class, TokenExpiredException.class,
      InvalidEmailTokenException.class, UnauthorizedException.class
  })
  public ResponseEntity<ApiResult<?>> handleInvalidCredentials(RuntimeException ex) {
    return newResponse(ex, HttpStatus.UNAUTHORIZED);
  }


  /*
   * HttpStatus.NOT_FOUND (404)
   * 찾을 수 없음
   */
  @ExceptionHandler({
      NotFoundEmailException.class, NotFoundUserException.class,
      NotFoundCommentException.class, NotFoundNotificationException.class,
      ChatRoomNotFoundException.class
  })
  public ResponseEntity<ApiResult<?>> handleNotFoundException(RuntimeException ex) {
    return newResponse(ex, HttpStatus.NOT_FOUND);
  }

  /**
   * 일반적인 예외 처리
   * @param ex Exception
   * @return ResponseEntity
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResult<?>> handleGeneralException(Exception ex) {
    ex.printStackTrace();
    return newResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
  }



}
