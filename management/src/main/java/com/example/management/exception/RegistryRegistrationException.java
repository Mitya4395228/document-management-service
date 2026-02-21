package com.example.management.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RegistryRegistrationException extends RuntimeException {

    public RegistryRegistrationException(String message) {
        super(message);
    }

}
