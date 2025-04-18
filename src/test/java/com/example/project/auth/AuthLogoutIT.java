package com.example.project.auth;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.repositories.UserRepository;
import com.example.project.test.AbstractTestContainersConnect;
import com.example.project.utils.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("Auth")
@Tag("logout")
public class AuthLogoutIT extends AbstractTestContainersConnect {

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

        UserDTO userDTO = new UserDTO("luntik", "qwerty");
        userRepository.save(new User(userDTO.getLogin(), passwordEncoder.encode(userDTO.getPassword()), Role.USER));

        MvcResult resultSignIn = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(userDTO.getLogin()))
                .andReturn();

        SecurityContext securityContextSignIn = securityContext(resultSignIn);
        assertThat(securityContextSignIn).isNotNull();
        assertThat(securityContextSignIn.getAuthentication().getName()).isEqualTo(userDTO.getLogin());

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

        UserDTO userDTO = new UserDTO("luntik", "qwerty");

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