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

    public void validate(UserDTO userDTO) {

        if (userRepository.findByLogin(userDTO.getLogin()).isPresent()) {
            throw new UniqueLoginException("Пользотваль с логином '" + userDTO.getLogin() + "' уже занят");
        }
    }
}
