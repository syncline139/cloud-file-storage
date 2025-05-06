package com.example.project.validations.impl;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.exceptions.auth.UsernameExistenceException;
import com.example.project.exceptions.auth.UniqueUsernameException;
import com.example.project.exceptions.auth.UserNotValidationException;
import com.example.project.repositories.UserRepository;
import com.example.project.validations.AuthValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
@Primary
public class AuthValidationImpl implements AuthValidation {

    private final UserRepository userRepository;

    @Override
    public void bindingResultErrors(BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg
                        .append(error.getDefaultMessage())
                        .append(";");
            }
            log.warn("Запрос от пользователя не прошел валидацию: {}", errorMsg);
            throw new UserNotValidationException(errorMsg.toString());
        }
        log.info("Запрос от пользователя прошел валидацию");
    }

    @Override
    public void usernameExistenceErrors(UserDTO userDTO) {
        final Optional<User> user = userRepository.findByUsername(userDTO.getUsername());
        if (user.isEmpty()) {
            throw new UsernameExistenceException("Неверный логин или пароль");
        }
    }

    @Override
    public void uniqueLoginErrors(UserDTO userDTO) {

        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            log.warn("Логин '{}' уже занят", userDTO.getUsername());
            throw new UniqueUsernameException(String.format("Пользователь с логином '%s' уже зарегистрирован",userDTO.getUsername()));
        }

        if (userDTO.getUsername().equals("anonymousUser")) {
            log.warn("Мимо челик");
        } else {
            log.info("Логин доступен для регистрации");
        }

    }
}
