package com.example.project.utils;

import com.example.project.dto.request.UserDTO;
import com.example.project.exceptions.LoginConflictException;
import com.example.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
