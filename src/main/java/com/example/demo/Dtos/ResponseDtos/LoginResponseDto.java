package com.example.demo.Dtos.ResponseDtos;

import com.example.demo.Models.User;
import lombok.Getter;

import java.util.Map;

@Getter
public class LoginResponseDto
{
    private UserResponseDto user;

    private Map<String, String> tokens;

    private String message = "Login succesful";

    public LoginResponseDto() {}

    public LoginResponseDto(User user, Map<String, String> tokens) {
        this.user = new UserResponseDto(user);
        this.tokens = tokens;
    }
}
