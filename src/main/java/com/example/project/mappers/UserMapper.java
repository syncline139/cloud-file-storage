package com.example.project.mappers;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;

public interface UserMapper {

    User convertToUser(UserDTO userDTO);
}
