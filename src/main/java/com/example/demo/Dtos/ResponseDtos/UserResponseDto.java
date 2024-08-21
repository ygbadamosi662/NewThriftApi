package com.example.demo.Dtos.ResponseDtos;

import com.example.demo.Enums.Gender;
import com.example.demo.Enums.Role;
import com.example.demo.Enums.Status;
import com.example.demo.Models.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto
{
    private long id;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String fname;

    private String lname;
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String email;

    private String phone;

    private LocalDateTime created_on;

    private LocalDateTime updated_on;


    public UserResponseDto(){}

    public UserResponseDto(User user)
    {
        this.id = user.getId();
        this.status = user.getStatus();
        this.role = user.getRole();
        this.fname = user.getFname();
        this.lname = user.getLname();
        this.gender = user.getGender();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.created_on = user.getCreatedOn();
        this.updated_on = user.getUpdatedOn();
    }
}
