package com.example.project.entity;

import com.example.project.dto.request.UserDTO;
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
import org.springframework.data.redis.core.RedisHash;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

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
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Логин может содержать только буквы и цифры")
    @Column(name = "username")
    private String username;

    @NotEmpty(message = "Пароль не должен быть пустым")
    @Size(min = PASSWORD_MIN_LENGTH,max = PASSWORD_MAX_LENGTH,message = "Пароля должен быть в диапазоне от 6 до 50 символом")
    @Column(name = "password_hash")
    private String password;

    @NotNull(message = "У юзера должна быть роль")
    @Enumerated(value = EnumType.STRING)
    @Column(name = "role")
    private Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public static User createUserFromDTO(UserDTO dto, PasswordEncoder encoder) {
        return new User(
                dto.getUsername(),
                encoder.encode(dto.getPassword()),
                Role.USER
        );
    }

}
