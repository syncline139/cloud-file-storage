package com.example.project.integration.auth;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.repositories.UserRepository;
import com.example.project.config.TestBeans;
import com.example.project.utils.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest(classes = TestBeans.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("Auth")
@Tag("Sign-up")
public class AuthSignUpIT {

    public static final String USERNAME = "luntik";
    public static final String PASSWORD = "qwerty";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MinioClient minioClient;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSignUpUserSuccessfully() throws Exception {
        UserDTO userDTO = new UserDTO(USERNAME, PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()))
                .andReturn();

        Optional<User> savedUser = userRepository.findByUsername(userDTO.getUsername());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getUsername()).isEqualTo(userDTO.getUsername());
        assertThat(savedUser.get().getRole()).isEqualTo(Role.USER);
        assertThat(passwordEncoder.matches(userDTO.getPassword(), savedUser.get().getPassword())).isTrue();

        SecurityContext securityContext = securityContext(result);
        assertThat(securityContext).isNotNull();
        assertThat(securityContext.getAuthentication().getName()).isEqualTo(userDTO.getUsername());

        assertThat(minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(USERNAME)
                .build())).isTrue();
    }

    @Test
    @Tag("login-validation")
    void shouldFailWhenLoginIsTaken() throws Exception {
        UserDTO userDTO = new UserDTO(USERNAME, PASSWORD);
        userRepository.save(new User(USERNAME, passwordEncoder.encode(PASSWORD), Role.USER));

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(String.format("Пользователь с логином '%s' уже зарегистрирован", userDTO.getUsername())))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        SecurityContext securityContext = securityContext(result);
        assertThat(securityContext).isNull();
    }

    @Test
    @Tag("login-validation")
    void shouldFailWhenLoginIsEmpty() throws Exception {
        UserDTO userDTO = new UserDTO("", PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Логин не должен быть пустым")))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        SecurityContext securityContext = securityContext(result);
        assertThat(securityContext).isNull();
    }

    @Test
    @Tag("login-validation")
    void shouldFailWhenLoginIsTooShort() throws Exception {
        UserDTO userDTO = new UserDTO("a", PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Логина должен быть в диапазоне от 5 до 30 символом")))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        SecurityContext securityContext = securityContext(result);
        assertThat(securityContext).isNull();
    }

    @Test
    @Tag("login-validation")
    void shouldFailWhenLoginHasInvalidCharacters() throws Exception {
        UserDTO userDTO = new UserDTO("!!!!!!!", PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Логин может содержать только буквы, цифры и подчёркивание")))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        SecurityContext securityContext = securityContext(result);
        assertThat(securityContext).isNull();
    }

    @Test
    @Tag("password-validation")
    void shouldFailWhenPasswordIsTooShort() throws Exception {
        UserDTO userDTO = new UserDTO(USERNAME, "qwert");

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Пароля должен быть в диапазоне от 6 до 60 символом")))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        SecurityContext securityContext = securityContext(result);
        assertThat(securityContext).isNull();
    }

    @Test
    @Tag("password-validation")
    void shouldFailWhenPasswordIsEmpty() throws Exception {
        UserDTO userDTO = new UserDTO(USERNAME, "");

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Пароль не должен быть пустым")))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        SecurityContext securityContext = securityContext(result);
        assertThat(securityContext).isNull();
    }

    private SecurityContext securityContext(MvcResult result) {

        return  (SecurityContext) result
                .getRequest()
                .getSession()
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

    }
}