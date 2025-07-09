package com.example.project.exceptions.auth;

public class UsernameExistenceException extends RuntimeException {
    public UsernameExistenceException(String message) {
        super(message);
    }
}
