package com.example.project.services.auth;

import com.example.project.exceptions.AuthenticationCredentialsNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogoutService {


    /**
     * Осуществляет выход пользователя из системы.
     * <p>
     * Проверяет, прошёл ли пользователь аутентификацию. Если да — очищает SecurityContext и завершает текущую сессию.
     * В противном случае выбрасывает исключение {@link AuthenticationCredentialsNotFoundException}.
     * </p>
     *
     * @param request HTTP-запрос, используемый для получения текущей сессии
     * @throws AuthenticationCredentialsNotFoundException если пользователь не был аутентифицирован
     */
    public void logout(HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            log.warn("Попытка выхода неаутентифицированного пользователя");
            throw new AuthenticationCredentialsNotFoundException();
        }

        log.info("Аутентифицированный пользователь: {}", auth.getName());

        SecurityContextHolder.clearContext();
        log.info("Контекст безопасности очищен");

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("Сессия успешно инвалидирована");
        } else {
            log.info("Сессия отсутствует");
        }
    }



}
