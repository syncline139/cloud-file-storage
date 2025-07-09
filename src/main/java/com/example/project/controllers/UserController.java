package com.example.project.controllers;

import com.example.project.annotations.profile.UserInfoDoc;
import com.example.project.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @UserInfoDoc
    @GetMapping("/me")
    public ResponseEntity<?> userLoginInfo() {
        UserDetails user = userService.getAuthenticatedUserDetails();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("username",user.getUsername()));
    }
}
