package com.example.project.utils;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.exceptions.LoginExistenceException;
import com.example.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoginExistenceValidator {
    
    private final UserRepository userRepository;

    /**
     * Кастомная валидация
     * <p>
     *   Ищем логин из {@link UserDTO} если не найден значит кидается {@link LoginExistenceException},
     *   значит такого пользователе нету в БД и нельзя аутентифицироватся под данным логином.
     * </p>
     * @param userDTO объект с данными пользователя (логин и пароль)
     */
    public void validation(UserDTO userDTO) {

      final Optional<User> user = userRepository.findByLogin(userDTO.getLogin());

      log.info("Попытка найти пользователя по логину '{}'", userDTO.getLogin());

        if (user.isEmpty()) {
            log.warn("Пользователь с логином '{}' не был найден",userDTO.getLogin());
            throw new LoginExistenceException("Неверный логин или пароль");
        }

        log.info("Пользотваль с логином '{}' был найден", userDTO.getLogin());
    }
}
