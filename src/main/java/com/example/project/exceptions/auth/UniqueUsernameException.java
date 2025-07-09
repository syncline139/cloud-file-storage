package com.example.project.exceptions.auth;

public class UniqueUsernameException extends RuntimeException {
    public UniqueUsernameException(String message) {
        super(message);
    }
}
