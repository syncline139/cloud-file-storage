package com.example.project.services.impl;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.exceptions.auth.AuthenticationCredentialsNotFoundException;
import com.example.project.exceptions.auth.SpongeBobSquarePants;
import com.example.project.exceptions.storage.BucketNotFoundException;
import com.example.project.repositories.UserRepository;
import com.example.project.services.AuthService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final MinioClient minioClient;

    @Transactional
    @Override
    public void registerAndAuthenticateUser(UserDTO userDTO, HttpServletRequest request) {
        log.info("Регистрация и аутентификация пользователя: {}", userDTO.getUsername());

        if (userDTO.getUsername().equals("anonymousUser")) {
            throw new SpongeBobSquarePants("Мимо челик");
        }

        final User user = User.createUserFromDTO(userDTO, passwordEncoder);
            userRepository.save(user);
            log.info("Пользователь '{}' сохранён в базу данных", user.getUsername());
            createBucketByUsername(user);
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
        log.debug("Контекст безопасности очищен");

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
                userDTO.getUsername(), userDTO.getPassword());

        Authentication authenticationUser = authenticationManager.authenticate(authentication);
        log.debug("Аутентификация пользователя '{}' прошла успешно", userDTO.getUsername());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationUser);
        SecurityContextHolder.setContext(context);
        log.debug("Создание и заполнение нового контекста прошли успешно");

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        log.info("Сессия для пользователя '{}' была успешно создана", userDTO.getUsername());
    }

    private void createBucketByUsername(User user) {
        String bucketName = toValidBucketName(user.getUsername(), user.getId());
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Бакет с названием '{}' создан", bucketName);
            } else {
                log.info("Бакет с названием '{}' уже существует", bucketName);
            }
        } catch (Exception e) {
            log.error("Не удалось создать бакет для пользователя '{}': {}", user.getUsername(), e.toString());
            throw new BucketNotFoundException("Не удалось создать бакет для нового пользователя");
        }
    }

    public static String toValidBucketName(String username, int userId) {
        return username
                .toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "")
                + "-" + userId;
    }
}
