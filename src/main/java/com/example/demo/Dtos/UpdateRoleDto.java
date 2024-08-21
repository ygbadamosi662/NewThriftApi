package com.example.demo.Dtos;

import com.example.demo.Enums.Role;
import com.example.demo.Enums.Status;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleDto
{
    @Enumerated(EnumType.STRING)
    private Role role = Role.ADMIN;

    @Size(min=11)
    @Email(message = "Must follow this pattern 'dfshghf@hdfgf.com'")
    @NotNull
    private String email;
}
