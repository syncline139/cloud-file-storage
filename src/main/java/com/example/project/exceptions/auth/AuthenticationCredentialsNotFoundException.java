package com.example.project.exceptions.auth;

public class AuthenticationCredentialsNotFoundException extends RuntimeException {
    public AuthenticationCredentialsNotFoundException() {
        super("Пользователь не авторизован");
    }
}
