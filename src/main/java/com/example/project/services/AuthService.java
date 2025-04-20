package com.example.project.services;

import com.example.project.dto.request.UserDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    void registerAndAuthenticateUser(UserDTO userDTO, HttpServletRequest request);

    void authenticate(UserDTO userDTO, HttpServletRequest request);

    void logout(HttpServletRequest request);
}
