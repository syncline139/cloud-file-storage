package com.example.project.validations;

import com.example.project.dto.request.UserDTO;
import org.springframework.validation.BindingResult;

public interface AuthValidation {

    void usernameExistenceErrors(UserDTO userDTO);

    void uniqueLoginErrors(UserDTO userDTO);
}
