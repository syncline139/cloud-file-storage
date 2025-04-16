package com.example.project.services.auth;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.repositories.UserRepository;
import com.example.project.utils.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignUpService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрирует нового пользователя и выполняет его аутентификацию.
     * <p>
     * Преобразует DTO в сущность, хеширует пароль с помощью {@link PasswordEncoder},
     * устанавливает роль {@link Role#USER} и сохраняет пользователя в базу данных.
     * После сохранения инициирует процесс аутентификации через {@link AuthenticationManager},
     * устанавливает {@link SecurityContext} и создаёт HTTP-сессию с сохранённым контекстом безопасности.
     * </p>
     *
     * @param userDTO объект с данными пользователя (логин и пароль)
     * @param request HTTP-запрос, из которого берётся или создаётся сессия
     */
    @Transactional
    public void save(UserDTO userDTO, HttpServletRequest request) {
      final User user = convertToUser(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDTO.getLogin(), userDTO.getPassword());
        Authentication authenticationUser = authenticationManager.authenticate(authentication);

        HttpSession session = request.getSession(true);

        SecurityContextHolder.getContext().setAuthentication(authenticationUser);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

    }

    // UserDTO --> User
    private User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }


}
