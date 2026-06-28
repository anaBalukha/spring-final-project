package com.example.homework.exception;

import lombok.Getter;

/**
 * Thrown when creating/updating an entity would violate a uniqueness rule
 * (e.g. duplicate email or username). Carries a message bundle key plus
 * arguments for localization, mirroring ResourceNotFoundException.
 */
@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public DuplicateResourceException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }
}
