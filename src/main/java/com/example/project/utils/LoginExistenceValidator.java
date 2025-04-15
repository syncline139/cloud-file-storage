package com.example.project.utils;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.exceptions.LoginExistenceException;
import com.example.project.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LoginExistenceValidator {
    
    private final UserRepository userRepository;

    public void validation(UserDTO userDTO) {

        Optional<User> login = userRepository.findByLogin(userDTO.getLogin());

        if (login.isEmpty()) {
            throw new LoginExistenceException("Пользотваля с логином  '" + userDTO.getLogin() + "' не существует");
        }
    }
}
