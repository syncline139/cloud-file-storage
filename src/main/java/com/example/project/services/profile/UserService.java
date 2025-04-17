package com.example.project.services.profile;

import com.example.project.exceptions.AuthenticationCredentialsNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    /**
     * Возвращает информацию об аутентифицированном пользователе.
     *<p>
     *     Извлекает текущий {@link Authentication} из {@link SecurityContextHolder},
     *     проверяет что пользотваль авторизированный и возваращет его {@link UserDetails},
     *     в случае отсутствия авторизации выбрасывает {@link AuthenticationCredentialsNotFoundException}.
     *</p>
     *
     * @return данные авторизированного пользователя
     */
    public UserDetails info() {

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
