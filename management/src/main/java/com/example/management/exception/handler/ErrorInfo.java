package com.example.management.exception.handler;

import java.util.List;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatusCode;

import com.example.management.util.ExceptionUtil;

/**
 * Used as DTO for exception handling {@link GlobalExceptionHandler}
 */

public record ErrorInfo(HttpStatusCode statusCode, String message) {

    public ErrorInfo(HttpStatusCode statusCode, List<? extends MessageSourceResolvable> messages) {
        this(statusCode, ExceptionUtil.getJointedMessages(messages));
    }

    public ErrorInfo(HttpStatusCode statusCode, Throwable throwable) {
        this(statusCode, throwable.getMessage());
    }

}
