package com.example.project.entity;

import com.example.project.utils.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Empty;

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
    private static final int PASSWORD_MAX_LENGTH = 60;

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
    @Column(name = "password_hash")
    private String password;

    @NotNull(message = "У юзера должна быть роль")
    @Enumerated(value = EnumType.STRING)
    @Column(name = "role")
    private Role role;
}
