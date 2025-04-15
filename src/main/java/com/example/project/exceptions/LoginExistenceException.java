package com.example.project.exceptions;

public class LoginExistenceException extends RuntimeException {
    public LoginExistenceException(String message) {
        super(message);
    }
}
