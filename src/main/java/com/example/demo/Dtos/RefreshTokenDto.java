package com.example.demo.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenDto
{
    @NotNull
    @NotBlank
    private String token;

    @NotNull
    private long userId;

}
