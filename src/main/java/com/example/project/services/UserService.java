package com.example.project.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    UserDetails getAuthenticatedUserDetails();
}
