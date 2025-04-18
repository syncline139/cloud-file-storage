package com.example.project.profile;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.repositories.UserRepository;
import com.example.project.test.AbstractTestContainersConnect;
import com.example.project.utils.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@Tag("profile")
public class userInfoIT extends AbstractTestContainersConnect {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @Tag("userLoginInfo")
    void shouldSuccessfullyRetrieveUserDetailsWhenAuthenticated() throws Exception {

        UserDTO userDTO = new UserDTO("luntik","qwerty");
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

        mockMvc.perform(get("/api/user/me")
                        .with(SecurityMockMvcRequestPostProcessors.securityContext(securityContextSignIn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(userDTO.getLogin()));
    }
    @Test
    @Tag("userLoginInfo")
    void shouldDenyUserDetailsAccessWhenNotAuthenticated() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Пользователь не авторизован"))
                .andExpect(jsonPath("$.statusCode").value(401))
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
