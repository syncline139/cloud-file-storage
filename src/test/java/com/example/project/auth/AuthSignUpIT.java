package com.example.project.auth;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.repositories.UserRepository;
import com.example.project.test.AbstractTestContainersConnect;
import com.example.project.utils.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("Auth")
@Tag("Sign-up")
public class AuthSignUpIT extends AbstractTestContainersConnect {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSignUpUserSuccessfully() throws Exception {
        UserDTO userDTO = new UserDTO("luntik", "qwerty");

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value(userDTO.getLogin()))
                .andReturn();

        Optional<User> savedUser = userRepository.findByLogin(userDTO.getLogin());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getLogin()).isEqualTo(userDTO.getLogin());
        assertThat(savedUser.get().getRole()).isEqualTo(Role.USER);
        assertThat(passwordEncoder.matches(userDTO.getPassword(), savedUser.get().getPassword())).isTrue();

        SecurityContext securityContext = securityContext(result);

        assertThat(securityContext).isNotNull();
        assertThat(securityContext.getAuthentication().getName()).isEqualTo(userDTO.getLogin());
    }

    @Test
    @Tag("login-validation")
    void shouldFailWhenLoginIsTaken() throws Exception {
        UserDTO userDTO = new UserDTO("luntik", "qwerty");
        userRepository.save(new User("luntik", passwordEncoder.encode("qwerty"), Role.USER));

        MvcResult result = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(String.format("Пользователь с логином '%s' уже зарегистрирован", userDTO.getLogin())))
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        SecurityContext securityContext = securityContext(result);
        assertThat(securityContext).isNull();
    }

    @Test
    @Tag("login-validation")
    void shouldFailWhenLoginIsEmpty() throws Exception {
        UserDTO userDTO = new UserDTO("", "qwerty");

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
        UserDTO userDTO = new UserDTO("a", "qwerty");

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
        UserDTO userDTO = new UserDTO("!!!!!!!", "qwerty");

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
        UserDTO userDTO = new UserDTO("luntik", "qwert");

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
        UserDTO userDTO = new UserDTO("luntik", "");

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