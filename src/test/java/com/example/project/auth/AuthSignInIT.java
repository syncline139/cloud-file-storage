package com.example.project.auth;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.repositories.UserRepository;
import com.example.project.config.TestBeans;
import com.example.project.utils.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestBeans.class)
@Transactional
@AutoConfigureMockMvc
@Tag("Auth")
@Tag("Sign-In")
@ActiveProfiles("test")
public class AuthSignInIT {

    public static final String USERNAME = "luntik";
    public static final String PASSWORD = "qwerty";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSignInUserSuccessfully() throws Exception {
        UserDTO userDTO = new UserDTO(USERNAME, PASSWORD);
        userRepository.save(new User(userDTO.getUsername(),passwordEncoder.encode(userDTO.getPassword()), Role.USER));

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()))
                .andReturn();

        SecurityContext securityContext = securityContext(result);

        assertThat(securityContext).isNotNull();
        assertThat(securityContext.getAuthentication().getName()).isEqualTo(userDTO.getUsername());
    }

    @Test
    @Tag("login-validation")
    void shouldFailWhenUserDoesNotExistOnLogin() throws Exception{
        UserDTO userDTO = new UserDTO(USERNAME,PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверный логин или пароль"))
                .andExpect(jsonPath("$.statusCode").value(401))
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
    void shouldFailWhenPasswordIsIncorrectOnLogin() throws Exception{
        UserDTO userDTO = new UserDTO(USERNAME,"sdsfsdafasdf");
        userRepository.save(new User(userDTO.getUsername(), passwordEncoder.encode(PASSWORD), Role.USER));

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверный логин или пароль"))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        SecurityContext securityContext = securityContext(result);

        assertThat(securityContext).isNull();
    }

    @Test
    @Tag("password-validation")
    void shouldFailWhenPasswordIsTooShortOnLogin() throws Exception{
        UserDTO userDTO = new UserDTO(USERNAME,"1");

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
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
    void shouldFailWhenPasswordIsEmptyOnLogin() throws Exception {
        UserDTO userDTO = new UserDTO(USERNAME,"");

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
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
