package com.example.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private static final int LOGIN_MAX_LENGTH = 36;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 60;

    @Schema(description = "Уникальный логин", example = "User1")
    @NotEmpty(message = "Логин не должен быть пустым")
    @Size(min = LOGIN_MIN_LENGTH, max = LOGIN_MAX_LENGTH, message = "Логина должен быть в диапазоне от 5 до 36 символом")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Логин может содержать только буквы и цифры")
    private String username;

    @Schema(description = "Пароль пользователя", example = "securePassword123")
    @NotEmpty(message = "Пароль не должен быть пустым")
    @Size(min = PASSWORD_MIN_LENGTH,max = PASSWORD_MAX_LENGTH,message = "Пароля должен быть в диапазоне от 6 до 60 символом")
    private String password;
}
