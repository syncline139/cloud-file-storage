package com.example.project.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private static final int LOGIN_MIN_LENGTH = 5;
    private static final int LOGIN_MAX_LENGTH = 30;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 50;

    @NotEmpty(message = "Логин не должен быть пустым")
    @Size(min = LOGIN_MIN_LENGTH, max = LOGIN_MAX_LENGTH, message = "Логина должен быть в диапазоне от 5 до 30 символом")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Логин может содержать только буквы, цифры и подчёркивание")
    @JsonProperty("login")
    private String login;

    @NotEmpty(message = "Пароль не должен быть пустым")
    @Size(min = PASSWORD_MIN_LENGTH,max = PASSWORD_MAX_LENGTH,message = "Пароля должен быть в диапазоне от 5 до 50 символом")
    @JsonProperty("password")
    private String password;
}
