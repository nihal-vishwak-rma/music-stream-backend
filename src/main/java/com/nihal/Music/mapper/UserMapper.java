package com.nihal.Music.mapper;

import com.nihal.Music.dtos.SignUpRequest;
import com.nihal.Music.dtos.SignUpResponse;
import com.nihal.Music.dtos.UserDto;
import com.nihal.Music.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @Autowired
    private ModelMapper modelMapper;


    public User signUpRequestToUser(SignUpRequest signUpRequest) {

        User user = this.modelMapper.map(signUpRequest, User.class);
        return user;
    }

    public SignUpResponse userToSignUpResponse(User user) {
        SignUpResponse signUpResponse = this.modelMapper.map(user, SignUpResponse.class);
        return signUpResponse;
    }

    public UserDto userToUserDto(User user) {
        UserDto userDto = this.modelMapper.map(user, UserDto.class);
        return userDto;
    }


}
