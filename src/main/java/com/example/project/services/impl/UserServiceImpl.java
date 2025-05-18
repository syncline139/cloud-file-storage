package com.example.project.services.impl;

import com.example.project.exceptions.auth.AuthenticationCredentialsNotFoundException;
import com.example.project.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class UserServiceImpl implements UserService {

    @Override
    public UserDetails getAuthenticatedUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.info("Попытка получить информацию о текущем пользователе");

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            log.warn("Пользователь не авторизован");
            throw new AuthenticationCredentialsNotFoundException();
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        log.info("Пользователь с логином '{}' авторизован", userDetails.getUsername());

        log.info("Пользотваль получил запрашиваемые данные");
        return userDetails;
    }
}
