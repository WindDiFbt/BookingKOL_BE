package com.web.bookingKol.common.exception;

import com.web.bookingKol.common.payload.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> illegalArgumentExceptionHandler(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(List.of(ex.getMessage()))
                                .build());
    }

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<ApiResponse<?>> validationExceptionHandler(ValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getErrors())
                                .build());
    }

    @ExceptionHandler(value = UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> UserAlreadyExistsExceptionHandler(UserAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .message(List.of(exception.getMessage()))
                                .build()
                );
    }

    @ExceptionHandler(value = RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> RoleNotFoundExceptionHandler(RoleNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(List.of(exception.getMessage()))
                                .build()
                );
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> HttpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                                .message(List.of(exception.getMessage()))
                                .build()
                );
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<ApiResponse<?>> NullPointerExceptionHandler(NullPointerException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(List.of(exception.getMessage()))
                                .build()
                );
    }

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<ApiResponse<?>> BindExceptionHandler(BindException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(exception.getAllErrors().stream()
                                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                        .toList())
                                .build()
                );
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> EntityNotFoundExceptionHandler(EntityNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(List.of(exception.getMessage()))
                                .build()
                );
    }

    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> MaxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException exception) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                                .message(List.of(exception.getMessage()))
                                .build()
                );
    }

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> UnsupportedMediaTypeExceptionHandler(HttpMediaTypeNotSupportedException exception) {
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                                .message(List.of(exception.getMessage()))
                                .build()
                );
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> UnsupportedMediaTypeExceptionHandler(ConstraintViolationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(List.of(exception.getMessage()))
                                .build()
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(List.of("Malformed JSON request" + exception))
                                .build()
                );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResultException(NoResourceFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(List.of("No static resource found: " + exception))
                                .build()
                );
    }
}
