package com.example.project.controllers;

import com.example.project.annotations.auth.UserSignInDoc;
import com.example.project.annotations.auth.UserSignOutDoc;
import com.example.project.annotations.auth.UserSignUpDoc;
import com.example.project.dto.request.UserDTO;
import com.example.project.services.AuthService;
import com.example.project.validations.AuthValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthValidation authValidation;

    @UserSignUpDoc
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String,String>> signUp(@RequestBody @Valid UserDTO userDTO,
                                    HttpServletRequest request) {
        authValidation.uniqueLoginErrors(userDTO);
        authService.registerAndAuthenticateUser(userDTO, request);
            return ResponseEntity
                .status(HttpStatus.CREATED) // 201
                .body(Map.of("username", userDTO.getUsername()));
    }

    @UserSignInDoc
    @PostMapping("/sign-in")
    public ResponseEntity<Map<String,String>> signIn(@RequestBody @Valid UserDTO userDTO,
                                    HttpServletRequest request) {
        authValidation.usernameExistenceErrors(userDTO);
        authService.authenticate(userDTO,request);
        return ResponseEntity
                .status(HttpStatus.OK)  // 200
                .body(Map.of("username", userDTO.getUsername()));
    }

    @UserSignOutDoc
    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204
    }

}
