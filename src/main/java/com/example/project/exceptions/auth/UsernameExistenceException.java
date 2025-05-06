package com.example.project.exceptions.auth;

/**
 * Исключение выбрасывается если пользователь пытается залогинится под неверными данными
 */
public class UsernameExistenceException extends RuntimeException {
    public UsernameExistenceException(String message) {
        super(message);
    }
}
