package com.example.project.exceptions;

public class UniqueLoginException extends RuntimeException {
    public UniqueLoginException(String message) {
        super(message);
    }
}
