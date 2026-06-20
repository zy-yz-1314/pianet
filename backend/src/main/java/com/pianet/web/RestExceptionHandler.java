package com.pianet.web;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class RestExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> responseStatus(ResponseStatusException ex) {
    Map<String, Object> body = buildBody(ex.getReason());
    return ResponseEntity.status(ex.getStatusCode()).body(body);
  }

  @ExceptionHandler(EntityExistsException.class)
  public ResponseEntity<Map<String, Object>> conflict(EntityExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(buildBody(ex.getMessage()));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Map<String, Object>> notFound(EntityNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildBody(ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> notReadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBody("请求体格式不正确"));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> methodNotSupported(HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(buildBody("不支持的请求方法"));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
  public ResponseEntity<Map<String, Object>> badRequest(Exception ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildBody("请求参数不合法"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> catchAll(Exception ex) {
    log.error("未预期的服务端错误", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(buildBody("服务内部错误，请稍后再试"));
  }

  private Map<String, Object> buildBody(String message) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", message);
    body.put("timestamp", Instant.now().toString());
    return body;
  }
}
