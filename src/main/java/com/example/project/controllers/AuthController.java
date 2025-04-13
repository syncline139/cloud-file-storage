package com.example.project.controllers;

import com.example.project.dto.request.UserDTO;
import com.example.project.dto.response.UserErrorResponse;
import com.example.project.exceptions.UserNotCreatedException;
import com.example.project.entity.User;
import com.example.project.services.auth.RegistrationService;
import com.example.project.utils.UserValidation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserValidation userValidation;
    private final RegistrationService registrationService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> registration(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {

        userValidation.validate(userDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg
                        .append(error.getDefaultMessage())
                        .append(";");
            }
            throw new UserNotCreatedException(errorMsg.toString());
        }

        registrationService.save(userDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("login", userDTO.getLogin())); // 201
    }



}
