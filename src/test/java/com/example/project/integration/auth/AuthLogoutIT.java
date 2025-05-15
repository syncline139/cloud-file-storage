package com.example.project.integration.auth;

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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Transactional
@SpringBootTest(classes = TestBeans.class)
@AutoConfigureMockMvc
@Tag("Auth")
@Tag("logout")
@ActiveProfiles("test")
public class AuthLogoutIT {

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


    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Tag("logout")
    void shouldSuccessfullyLogoutFromAccount() throws Exception {

        UserDTO userDTO = new UserDTO(USERNAME, PASSWORD);
        userRepository.save(new User(userDTO.getUsername(), passwordEncoder.encode(userDTO.getPassword()), Role.USER));

        MvcResult resultSignIn = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userDTO.getUsername()))
                .andReturn();

        SecurityContext securityContextSignIn = securityContext(resultSignIn);
        assertThat(securityContextSignIn).isNotNull();
        assertThat(securityContextSignIn.getAuthentication().getName()).isEqualTo(userDTO.getUsername());

        MvcResult resultLogout = mockMvc.perform(post("/api/auth/sign-out")
                        .with(SecurityMockMvcRequestPostProcessors.securityContext(securityContextSignIn)))
                .andExpect(status().isNoContent())
                .andReturn();

        SecurityContext securityContextSignOut = securityContext(resultLogout);
        assertThat(securityContextSignOut).isNull();
    }

    @Test
    @Tag("logout")
    void shouldFailLogoutWhenUserIsNotAuthenticated() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/sign-out"))
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(status().isUnauthorized())
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