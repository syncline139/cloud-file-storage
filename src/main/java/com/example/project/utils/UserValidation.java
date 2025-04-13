package com.example.project.utils;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.exceptions.UserNotFoundException;
import com.example.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Component
public class UserValidation implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDTO userDTO = (UserDTO) target;

        if (userRepository.findByLogin(userDTO.getLogin()).isPresent()) {
            errors.rejectValue("login","", "Пользователь с логином '" + userDTO.getLogin() + "' уже зарегистрирован");
        }
    }
}
