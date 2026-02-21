package com.example.management.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UnacceptableStatusException extends RuntimeException {

    public UnacceptableStatusException(String message) {
        super(message);
    }

}
