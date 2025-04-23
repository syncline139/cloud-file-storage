package com.example.project.services.impl;

import com.example.project.config.MinioConfig;
import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.exceptions.auth.AuthenticationCredentialsNotFoundException;
import com.example.project.exceptions.auth.SpongeBobSquarePants;
import com.example.project.exceptions.storage.BucketNotFoundException;
import com.example.project.mappers.UserMapper;
import com.example.project.repositories.UserRepository;
import com.example.project.services.AuthService;
import com.example.project.utils.Role;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TransactionTemplate transactionTemplate;
    private final MinioClient minioClient;


    @Override
    public void registerAndAuthenticateUser(UserDTO userDTO, HttpServletRequest request) {

        if (userDTO.getLogin().equals("anonymousUser")) {
            throw new SpongeBobSquarePants("Мимо челик");
        }

        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(status -> {
        final User user = userMapper.convertToUser(userDTO);
        log.info("Конвертация userDTO в сущность User прошла успешно");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Пароль пользователя был успешно захеширован");

        user.setRole(Role.USER);
        log.info("Пользователю присвоена роль USER");

            try {
                if (!minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(user.getLogin())
                        .build())) {
                    userRepository.save(user);
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(user.getLogin())
                            .build());
                    log.info("Пользователь '{}' сохранён в базу данных", user.getLogin());
                    log.info("Бакет с названием '{}' успешно создан", user.getLogin());
                } else {
                    log.info("Бакет с названием '{}' уже существует", user.getLogin());
                }
            } catch (Exception e) {
                log.error("Не удалось создать бакет для пользователя '{}': {}", user.getLogin(), e.getMessage());
                throw new BucketNotFoundException("Не удалось создать бакет для нового пользователя");
            }

            return null;
        });

        setupAuthenticationAndSession(userDTO,request);
    }

    @Override
    public void authenticate(UserDTO userDTO, HttpServletRequest request) {

        setupAuthenticationAndSession(userDTO,request);
    }

    @Override
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

    private void setupAuthenticationAndSession(UserDTO userDTO, HttpServletRequest request) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDTO.getLogin(), userDTO.getPassword());

        Authentication authenticationUser = authenticationManager.authenticate(authentication);

        log.info("Аутентификация пользователя '{}' прошла успешно", userDTO.getLogin());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationUser);
        SecurityContextHolder.setContext(context);
        log.info("Создание и заполнение нового контекста прошли успешно");

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        log.info("Сессия для пользователя '{}' была успешно создана", userDTO.getLogin());
    }
}
