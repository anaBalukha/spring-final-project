package com.example.homework.exception;

import lombok.Getter;

/**
 * Thrown when a requested entity does not exist. Carries a message bundle
 * key plus arguments so the GlobalExceptionHandler can localize the
 * response based on the request's Accept-Language header.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public ResourceNotFoundException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }
}
