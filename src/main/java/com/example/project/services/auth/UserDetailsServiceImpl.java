package com.example.project.services.auth;


import com.example.project.entity.User;
import com.example.project.exceptions.UniqueLoginException;
import com.example.project.repositories.UserRepository;
import com.example.project.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsImpl loadUserByUsername(String login) throws UsernameNotFoundException {

        Optional<User> maybeUser = userRepository.findByLogin(login);

        if (maybeUser.isEmpty()) {
            throw new UniqueLoginException("Пользотваль с логином: " + login + " не найден");
        }

        return new UserDetailsImpl(maybeUser.get());
    }
}
