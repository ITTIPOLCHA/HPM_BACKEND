package com.gj.hpm.config.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
public class FoundException extends RuntimeException {
    public FoundException(String message) {
        super(message);
    }
}