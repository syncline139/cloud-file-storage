package com.example.project.exceptions;

public class UserNotValidationException extends RuntimeException {
    public UserNotValidationException(String message) {
        super(message);
    }
}
