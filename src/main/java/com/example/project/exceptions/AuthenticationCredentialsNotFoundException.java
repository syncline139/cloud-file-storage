package com.example.project.exceptions;

/**
 * Исключение выбрасывается если пользователь не авторизирован
 */
public class AuthenticationCredentialsNotFoundException extends RuntimeException {
    public AuthenticationCredentialsNotFoundException() {
        super("Пользователь не авторизован");
    }
}
