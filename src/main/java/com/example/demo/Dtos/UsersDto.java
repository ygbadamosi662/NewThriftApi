package com.example.demo.Dtos;

import com.example.demo.Enums.Gender;
import com.example.demo.Enums.Role;
import com.example.demo.Enums.Status;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersDto
{
    private boolean count = false;

    @Size(min=3,max=25)
    private String fname;

    @Size(min=3,max=25)
    private String lname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Email(message = "Enter a valid email, eg: example6_@gmail.com",
            regexp = "[a-z]{2,30}@[a-z]{2,10}\\.[a-z]{2,10}")
    private String email;

    @Size(min=10,max=10)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private int lastHours = 0;

    private int page = 1;

    private int size = 3;

    public boolean getCount() {
        return this.count;
    }
}
