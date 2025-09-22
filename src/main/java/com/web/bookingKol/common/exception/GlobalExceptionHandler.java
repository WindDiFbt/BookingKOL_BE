package com.web.bookingKol.common.exception;

import com.web.bookingKol.common.payload.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}
