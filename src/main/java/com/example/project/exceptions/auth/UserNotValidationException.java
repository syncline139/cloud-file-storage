package com.example.project.exceptions.auth;

public class UserNotValidationException extends RuntimeException {
    public UserNotValidationException(String message) {
        super(message);
    }
}
