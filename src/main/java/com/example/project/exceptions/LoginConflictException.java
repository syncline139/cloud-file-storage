package com.example.project.exceptions;

public class LoginConflictException extends RuntimeException {
    public LoginConflictException(String message) {
        super(message);
    }
}
