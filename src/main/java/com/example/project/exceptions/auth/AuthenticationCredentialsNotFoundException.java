package com.example.project.exceptions.auth;

/**
 * Исключение выбрасывается если пользователь не авторизирован
 */
public class AuthenticationCredentialsNotFoundException extends RuntimeException {
    public AuthenticationCredentialsNotFoundException() {
        super("Пользователь не авторизован");
    }
}
