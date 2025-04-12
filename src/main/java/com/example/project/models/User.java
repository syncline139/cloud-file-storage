package com.example.project.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private static final int LOGIN_MIN_LENGTH = 5;
    private static final int LOGIN_MAX_LENGTH = 30;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotEmpty(message = "Логин не должен быть пустым")
    @Size(min = LOGIN_MIN_LENGTH, max = LOGIN_MAX_LENGTH, message = "Логина должен быть в диапазоне от 5 до 30 символом")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Логин может содержать только буквы, цифры и подчёркивание")
    @Column(name = "login")
    private String login;

    @NotEmpty(message = "Пароль не должен быть пустым")
    @Size(min = PASSWORD_MIN_LENGTH,max = PASSWORD_MAX_LENGTH,message = "Пароля должен быть в диапазоне от 5 до 50 символом")
    @Column(name = "password_hash")
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @NotEmpty(message = "У юзера должна быть роль")
    @Column(name = "role")
    private String role;
}
