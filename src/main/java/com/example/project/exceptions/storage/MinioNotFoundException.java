package com.example.project.exceptions.storage;

public class MinioNotFoundException extends RuntimeException {
    public MinioNotFoundException(String message) {
        super(message);
    }
}
