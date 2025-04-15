package com.example.project.controllers;

import com.example.project.dto.request.UserDTO;
import com.example.project.services.auth.LoginService;
import com.example.project.services.auth.LogoutService;
import com.example.project.services.auth.RegistrationService;
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
    private final RegistrationService registrationService;
    private final BindingResultValidator bindingResultValidator;
    private final LoginExistenceValidator loginExistenceValidator;
    private final LoginService loginService;
    private final LogoutService logoutService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> registration(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult, HttpServletRequest request) {

        uniqueLoginValidation.validate(userDTO);

        bindingResultValidator.checkForValidationErrors(bindingResult);

        registrationService.save(userDTO, request);

            return ResponseEntity
                .status(HttpStatus.CREATED) // 201
                .body(Map.of("login", userDTO.getLogin()));
    }


    @PostMapping("/sign-in")
    public ResponseEntity<?> authentication(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult, HttpServletRequest request) {

        loginExistenceValidator.validation(userDTO);

        bindingResultValidator.checkForValidationErrors(bindingResult);

        loginService.authentication(userDTO,request);

        return ResponseEntity
                .status(HttpStatus.OK)  // 200
                .body(Map.of("login", userDTO.getLogin()));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<HttpStatus> logout(HttpServletRequest request) {

        logoutService.logout(request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
