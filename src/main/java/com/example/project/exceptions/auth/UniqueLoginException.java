package com.example.project.exceptions.auth;

/**
 * Исключение выбрасывается если пользотель пытается зарегистроватся с уже занятым логином
 */
public class UniqueLoginException extends RuntimeException {
    public UniqueLoginException(String message) {
        super(message);
    }
}
