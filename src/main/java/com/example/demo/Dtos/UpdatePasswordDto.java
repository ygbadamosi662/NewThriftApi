package com.example.demo.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordDto
{
    @NotNull
    @NotBlank
    private String token;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?!.*\\s).{8,}$",
            message = "Password must be atleast 8 characters long and contain " +
                    "at least one uppercase letter, one lowercase letter, " +
                    "one digit, and one special character.")
    private String new_password;
}
