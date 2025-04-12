package com.example.project.utils;

import com.example.project.entity.User;
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
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        if (userRepository.findByLogin(user.getLogin()).isPresent()) {
            throw new UsernameNotFoundException("Пользотваль с логином '" + user.getLogin() + "' уже зарегистрирован");
        }
    }
}
