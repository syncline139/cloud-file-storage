package com.example.project.mappers.impl;

import com.example.project.dto.request.UserDTO;
import com.example.project.entity.User;
import com.example.project.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

    private final ModelMapper modelMapper;

    @Override
    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}
