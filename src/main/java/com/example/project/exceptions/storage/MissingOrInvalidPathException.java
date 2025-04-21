package com.example.project.exceptions.storage;

public class MissingOrInvalidPathException extends RuntimeException {
    public MissingOrInvalidPathException(String message) {
        super(message);
    }
}
