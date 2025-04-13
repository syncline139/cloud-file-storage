package com.example.project.utils;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.exceptions.LoginConflictException;
import com.example.project.exceptions.UserNotFoundException;
import com.example.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@RequiredArgsConstructor
@Component
public class UserValidation {

    private final UserRepository userRepository;

    public void validate(UserDTO userDTO) {

        if (userRepository.findByLogin(userDTO.getLogin()).isPresent()) {
            throw new LoginConflictException("Пользотваль с логином '" + userDTO.getLogin() + "' уже занят");
        }
    }
}
