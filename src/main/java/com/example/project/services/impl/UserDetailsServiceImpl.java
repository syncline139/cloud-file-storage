package com.example.project.services.impl;


import com.example.project.entity.User;
import com.example.project.exceptions.auth.UniqueUsernameException;
import com.example.project.repositories.UserRepository;
import com.example.project.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDetailsImpl loadUserByUsername(String login) throws UsernameNotFoundException {
      final  Optional<User> maybeUser = userRepository.findByUsername(login);
       log.info("Попытка найти пользователя с логином '{}'",login);
        if (maybeUser.isEmpty()) {
            log.warn("Не удалось найти пользователя с логином '{}' ", login);
            throw new UniqueUsernameException(String.format("Пользотваль с логином: '%s' не найден", login));
        }
        log.info("Пользователя с логином '{}' найден",login);
        return new UserDetailsImpl(maybeUser.get());
    }
}
