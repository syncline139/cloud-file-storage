package com.example.project.utils;

import com.example.project.dto.request.UserDTO;
import com.example.project.exceptions.UniqueLoginException;
import com.example.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UniqueLoginValidation {

    private final UserRepository userRepository;

    /**
     * Кастомная валидация
     * <p>
     *   Ищем логин из {@link UserDTO} если найден значит кидается {@link UniqueLoginException},
     *   значит логин уже занят и новый пользотваль не сможет под ним зарегистироватся
     * </p>
     * @param userDTO объект с данными пользователя (логин и пароль)
     */
    public void validate(UserDTO userDTO) {

        if (userRepository.findByLogin(userDTO.getLogin()).isPresent()) {
            log.warn("Логин '{}' уже занят", userDTO.getLogin());
            throw new UniqueLoginException("Пользователь с логином '" + userDTO.getLogin() + "' уже зарегистрирован");
        }

        log.info("Логин '{}' доступен для регистрации", userDTO.getLogin());
    }

}
