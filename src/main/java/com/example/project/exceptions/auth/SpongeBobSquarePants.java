package com.example.project.exceptions.auth;

/**
 * Исключение кидается если пользователь пытается зарегистрироваться с логином 'anonymousUser'
 * P.S. Просто прикол ;)
 */
public class SpongeBobSquarePants extends RuntimeException {
    public SpongeBobSquarePants(String message) {
        super(message);
    }
}
