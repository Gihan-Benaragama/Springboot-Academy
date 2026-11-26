package com.springbootacademy.api.exception;

/** Thrown when a unique value (student email, course code) already exists. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
