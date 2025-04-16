package com.example.project.services.auth;

import com.example.project.dto.request.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationUser);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
