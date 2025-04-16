package com.example.project.services.profile;

import com.example.project.exceptions.AuthenticationCredentialsNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException();
        }

       return  (UserDetails) auth.getPrincipal();
    }
}
