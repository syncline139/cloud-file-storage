package com.example.project.exceptions.storage;

public class MinioNotFoundException extends RuntimeException {
    public MinioNotFoundException(String message,Throwable cause) {
        super(message);
    }
}
