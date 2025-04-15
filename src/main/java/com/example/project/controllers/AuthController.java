package com.example.project.controllers;

import com.example.project.dto.request.UserDTO;
import com.example.project.services.auth.RegistrationService;
import com.example.project.services.auth.ValidationService;
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
    private final ValidationService validationService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> registration(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult, HttpServletRequest request) {

        uniqueLoginValidation.validate(userDTO);

        validationService.checkForValidationErrors(bindingResult);

        registrationService.save(userDTO, request);

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("login", userDTO.getLogin())); // 201
    }


//    @PostMapping("/sign-in")
//    public ResponseEntity<?> authentication(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
//
//        loginExistenceValidator.validation(userDTO);
//
//        validationService.checkForValidationErrors(bindingResult);
//
//
//
//
//    }



}
