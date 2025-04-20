package com.example.project.exceptions.auth;

/**
 * Исключение выбрасывается для обработки ошибок валидации пришедшей с сущности или DTO
 */
public class UserNotValidationException extends RuntimeException {
    public UserNotValidationException(String message) {
        super(message);
    }
}
