package com.example.project.exceptions.auth;

/**
 * Исключение выбрасывается если пользотель пытается зарегистроватся с уже занятым логином
 */
public class UniqueUsernameException extends RuntimeException {
    public UniqueUsernameException(String message) {
        super(message);
    }
}
