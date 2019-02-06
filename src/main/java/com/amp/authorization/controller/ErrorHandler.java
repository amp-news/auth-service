package com.amp.authorization.controller;

import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;

import com.amp.authorization.core.exception.ExternalServiceException;
import com.amp.authorization.model.dto.ErrorTO;
import com.amp.authorization.service.exception.TokenExpirationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestControllerAdvice
@ResponseBody
public class ErrorHandler {

  private static final String AUTH_ERROR = "Not authorized. ";
  private static final String DTO_VALIDATION_MSG_TEMPLATE = "Property %s %s.";

  @ExceptionHandler(Exception.class)
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  ErrorTO handleAny(Exception exception) {
    return new ErrorTO(exception.getMessage(), INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ExternalServiceException.class)
  ResponseEntity<ErrorTO> handleAny(ExternalServiceException exception) {
    final ErrorTO error = new ErrorTO(exception.getStatus());
    error.setMessages(exception.getMessages());
    error.setPath(exception.getPath());

    return ResponseEntity.status(exception.getStatus()).body(error);
  }

  @ExceptionHandler(InsufficientAuthenticationException.class)
  ResponseEntity<ErrorTO> handleAny(InsufficientAuthenticationException exception) {
    final ErrorTO error = new ErrorTO(exception.getMessage(), BAD_REQUEST);
    return ResponseEntity.status(BAD_REQUEST).body(error);
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<ErrorTO> handleAny(AuthenticationException exception) {
    final ErrorTO error = new ErrorTO(AUTH_ERROR + exception.getMessage(), UNAUTHORIZED);
    return ResponseEntity.status(UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(TokenExpirationException.class)
  ResponseEntity<ErrorTO> handleAny(TokenExpirationException exception) {
    final ErrorTO error = new ErrorTO(exception.getMessage(), BAD_REQUEST);
    return ResponseEntity.status(BAD_REQUEST).body(error);
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorTO> handleAny(AccessDeniedException exception) {
    final ErrorTO error = new ErrorTO(exception.getMessage(), FORBIDDEN);
    return ResponseEntity.status(FORBIDDEN).body(error);
  }

  // BASIC VALIDATION HANDLERS

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  ErrorTO handle(ConstraintViolationException exception) {
    final ErrorTO errorsTO = new ErrorTO(UNPROCESSABLE_ENTITY);
    errorsTO.setMessages(
        exception
            .getConstraintViolations()
            .stream()
            .map(
                constraintViolation ->
                    String.format(
                        "%s: %s",
                        constraintViolation.getPropertyPath(), constraintViolation.getMessage()))
            .collect(Collectors.toList()));

    return errorsTO;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  ErrorTO handle(MethodArgumentNotValidException exception) {
    final ErrorTO errorsTO = new ErrorTO(UNPROCESSABLE_ENTITY);
    errorsTO.setMessages(
        exception
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(
                fieldError ->
                    String.format(
                        DTO_VALIDATION_MSG_TEMPLATE,
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
            .collect(Collectors.toList()));

    return errorsTO;
  }

  // BASIC HANDLERS

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(BAD_REQUEST)
  ErrorTO handle(HttpMessageNotReadableException exception) {
    return new ErrorTO(exception.getMessage(), BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(BAD_REQUEST)
  ErrorTO handle(MethodArgumentTypeMismatchException exception) {
    return new ErrorTO(exception.getMessage(), BAD_REQUEST);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(METHOD_NOT_ALLOWED)
  ErrorTO handle(HttpRequestMethodNotSupportedException exception) {
    return new ErrorTO(exception.getMessage(), METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  @ResponseStatus(BAD_REQUEST)
  ErrorTO handle(UsernameNotFoundException exception) {
    return new ErrorTO(exception.getMessage(), BAD_REQUEST);
  }
}
