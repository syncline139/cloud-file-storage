package com.example.project.exceptions.auth;

/**
 * Исключение выбрасывается если пользователь пытается залогинится под неверными данными
 */
public class LoginExistenceException extends RuntimeException {
    public LoginExistenceException(String message) {
        super(message);
    }
}
