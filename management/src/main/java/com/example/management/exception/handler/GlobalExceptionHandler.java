package com.example.management.exception.handler;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        var httpStatus = status != null ? status.code() : HttpStatus.INTERNAL_SERVER_ERROR;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        var httpStatus = HttpStatus.BAD_REQUEST;
        var errorInfo = new ErrorInfo(httpStatus, ex.getFieldErrors());
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        var httpStatus = HttpStatus.BAD_REQUEST;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Object> handleHandlerMethodValidation(HandlerMethodValidationException ex,
            WebRequest request) {
        var httpStatus = HttpStatus.BAD_REQUEST;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    private void logError(ErrorInfo errorInfo) {
        log.error("GlobalExceptionHandler processed the error: {}", errorInfo.toString());
    }

    
}
