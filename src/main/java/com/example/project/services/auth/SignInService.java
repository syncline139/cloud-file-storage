package com.example.project.services.auth;

import com.example.project.dto.request.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignInService {

    private final AuthenticationManager authenticationManager;

    /**
     * Выполняет аутентификацию пользователя.
     * <p>
     * Создаёт {@link UsernamePasswordAuthenticationToken} на основе данных, полученных от клиента,
     * проходит процесс аутентификации через {@link AuthenticationManager}, устанавливает
     * {@link SecurityContext}, а также инициирует HTTP-сессию с сохранённым контекстом безопасности.
     * </p>
     *
     * @param userDTO объект с данными пользователя (логин и пароль)
     * @param request HTTP-запрос, из которого берётся или создаётся сессия
     */

    public void authentication(UserDTO userDTO, HttpServletRequest request) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDTO.getLogin(), userDTO.getPassword());
        Authentication authenticationUser = authenticationManager.authenticate(authentication);

        log.info("Аутентификация пользователя '{}' прошла успешно",userDTO.getLogin());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationUser);
        SecurityContextHolder.setContext(context);

        log.info("Создание и заполнения нового контекста прошла успешно");

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        log.info("Сессия для пользователя '{}' была успешно создана",userDTO.getLogin());
    }
}
