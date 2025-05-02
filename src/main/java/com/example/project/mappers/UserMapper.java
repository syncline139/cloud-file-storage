package com.example.project.mappers;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import org.springframework.web.bind.annotation.Mapping;

public interface UserMapper {

    User convertToUser(UserDTO userDTO);
}
