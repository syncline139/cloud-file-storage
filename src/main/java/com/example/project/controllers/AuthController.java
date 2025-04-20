package com.example.project.controllers;

import com.example.project.annotations.auth.UserSignInDoc;
import com.example.project.annotations.auth.UserSignOutDoc;
import com.example.project.annotations.auth.UserSignUpDoc;
import com.example.project.dto.request.UserDTO;
import com.example.project.services.AuthService;
import com.example.project.utils.BindingResultValidator;
import com.example.project.utils.LoginExistenceValidator;
import com.example.project.utils.UniqueLoginValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final UniqueLoginValidation uniqueLoginValidation;
    private final BindingResultValidator bindingResultValidator;
    private final LoginExistenceValidator loginExistenceValidator;
    private final AuthService authService;


    @UserSignUpDoc
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult, HttpServletRequest request) {

        bindingResultValidator.checkForValidationErrors(bindingResult);

        uniqueLoginValidation.validate(userDTO);


        authService.registerAndAuthenticateUser(userDTO, request);

            return ResponseEntity
                .status(HttpStatus.CREATED) // 201
                .body(Map.of("login", userDTO.getLogin()));
    }


    @UserSignInDoc
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult, HttpServletRequest request) {

        bindingResultValidator.checkForValidationErrors(bindingResult);

        loginExistenceValidator.validation(userDTO);


        authService.authenticate(userDTO,request);

        return ResponseEntity
                .status(HttpStatus.OK)  // 200
                .body(Map.of("login", userDTO.getLogin()));
    }

    @UserSignOutDoc
    @PostMapping("/sign-out")
    public ResponseEntity<HttpStatus> logout(HttpServletRequest request) {

        authService.logout(request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204
    }

}
