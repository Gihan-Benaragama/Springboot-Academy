package com.springbootacademy.api.exception;

/** Thrown when a business rule is violated (e.g. no seats available). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
