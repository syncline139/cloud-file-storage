package com.example.project.utils;

import com.example.project.dto.request.UserDTO;
import com.example.project.exceptions.UniqueLoginException;
import com.example.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
            throw new UniqueLoginException("Пользотваль с логином '" + userDTO.getLogin() + "' уже зарегистрирован");
        }
    }
}
