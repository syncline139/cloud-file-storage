package com.example.project.validations.impl;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.exceptions.auth.LoginExistenceException;
import com.example.project.exceptions.auth.UniqueLoginException;
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
    public void loginExistenceErrors(UserDTO userDTO) {
        final Optional<User> user = userRepository.findByLogin(userDTO.getLogin());
        if (user.isEmpty()) {
            throw new LoginExistenceException("Неверный логин или пароль");
        }
    }

    @Override
    public void uniqueLoginErrors(UserDTO userDTO) {

        if (userRepository.findByLogin(userDTO.getLogin()).isPresent()) {
            log.warn("Логин '{}' уже занят", userDTO.getLogin());
            throw new UniqueLoginException(String.format("Пользователь с логином '%s' уже зарегистрирован",userDTO.getLogin()));
        }

        if (userDTO.getLogin().equals("anonymousUser")) {
            log.warn("Мимо челик");
        } else {
            log.info("Логин доступен для регистрации");
        }

    }
}
