package com.sns.project.common.api;

import jakarta.validation.ConstraintViolationException;
import com.sns.project.core.exception.badRequest.InvalidFormatRequestException;
import com.sns.project.core.exception.conflict.ProductStateConflictException;
import com.sns.project.core.exception.forbidden.ForbiddenException;
import com.sns.project.core.exception.notfound.NotFoundUserException;
import com.sns.project.core.exception.unauthorized.InvalidPasswordException;
import com.sns.project.core.exception.unauthorized.InvalidEmailTokenException;
import com.sns.project.core.exception.notfound.ChatRoomNotFoundException;
import com.sns.project.core.exception.notfound.NotFoundCommentException;
import com.sns.project.core.exception.notfound.NotFoundEmailException;
import com.sns.project.core.exception.notfound.NotFoundNotificationException;
import com.sns.project.core.exception.notfound.NotFoundProductException;
import com.sns.project.core.exception.badRequest.RegisterFailedException;
import com.sns.project.core.exception.duplication.DuplicatedMessageException;
import com.sns.project.core.exception.unauthorized.TokenExpiredException;
import com.sns.project.core.exception.unauthorized.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.persistence.OptimisticLockException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ApiResult<?>> newResponse(Throwable throwable, HttpStatus httpStatus) {
    // logger.error("Exception occurred - Type: {}, Message: {}, Status: {}", 
    //     throwable.getClass().getSimpleName(), 
    //     throwable.getMessage(), 
    //     httpStatus)

    logger.error("Exception occurred - Type: {}, Message: {}, Status: {}", 
        throwable.getClass().getSimpleName(), 
        truncateMessage(throwable.getMessage()), 
        httpStatus);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity<>(ApiResult.error(throwable, httpStatus), headers, httpStatus);
  }

  private ResponseEntity<ApiResult<?>> newMessageResponse(String message, HttpStatus httpStatus) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity<>(ApiResult.error(message, httpStatus), headers, httpStatus);
  }

  private String truncateMessage(String message) {
    if (message == null || message.isBlank()) {
      return "no message";
    }
    return message.length() > 100 ? message.substring(0, 100) : message;
  }

  /*
   * HttpStatus.CONFLICT (409)  
   * 동시 수정 충돌, 잘못된 상태 전이, 중복 메시지
   */
  @ExceptionHandler({
    DuplicatedMessageException.class,
    ProductStateConflictException.class,
    ObjectOptimisticLockingFailureException.class,
    OptimisticLockException.class
  })
  public ResponseEntity<?> handleConflict(RuntimeException ex) {
    return newResponse(ex, HttpStatus.CONFLICT);
  }

  /*
   * HttpStatus.BAD_REQUEST (400)
   * 잘못된 요청
   */
  @ExceptionHandler({
      RegisterFailedException.class,
      InvalidFormatRequestException.class
  })
  public ResponseEntity<?> handleBadRequest(RuntimeException ex) {
    return newResponse(ex, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "잘못된 요청입니다.")
        .orElse("잘못된 요청입니다.");
    return newMessageResponse(message, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({
      ConstraintViolationException.class, HttpMessageNotReadableException.class
  })
  public ResponseEntity<?> handleValidationRelatedException(Exception ex) {
    String message = ex instanceof ConstraintViolationException constraintViolationException
        ? constraintViolationException.getConstraintViolations().stream()
            .findFirst()
            .map(violation -> violation.getMessage())
            .orElse("잘못된 요청입니다.")
        : "요청 형식이 올바르지 않습니다.";
    return newMessageResponse(message, HttpStatus.BAD_REQUEST);
  }


  /*
   * HttpStatus.FORBIDDEN (403)
   * 인가 실패
   */
  @ExceptionHandler({
      ForbiddenException.class
  })
  public ResponseEntity<?> handleForbidden(RuntimeException ex) {
    return newResponse(ex, HttpStatus.FORBIDDEN);
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
      ChatRoomNotFoundException.class, NotFoundProductException.class
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
    return newResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
  }



}
