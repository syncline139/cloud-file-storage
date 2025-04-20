package com.example.project.exceptions.storage;

public class BucketNotFoundException extends RuntimeException {
    public BucketNotFoundException(String message) {
        super(message);
    }
}
