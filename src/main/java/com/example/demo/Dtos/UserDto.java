package com.example.demo.Dtos;

import com.example.demo.Enums.Gender;
import com.example.demo.Enums.Role;
import com.example.demo.Enums.Status;
import com.example.demo.Models.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto
{
    @Size(min=3,max=25)
    @NotNull
    @NotBlank
    private String fname;

    @Size(min=3,max=25)
    @NotNull
    @NotBlank
    private String lname;

    @NotBlank
    @NotNull
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?!.*\\s).{8,}$",
            message = "Password must be atleast 8 characters long and contain " +
                    "at least one uppercase letter, one lowercase letter, " +
                    "one digit, and one special character.")
    private String password;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Email(message = "Enter a valid email, eg: example6_@gmail.com",
            regexp = "[a-z]{2,30}@[a-z]{2,10}\\.[a-z]{2,10}")
    @NotNull
    private String email;

    @NotNull
    @NotBlank
    @Size(min=10,max=10)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    public User getUser ()
    {
        User user = new User();
        user.setEmail(this.email);
        user.setFname(this.fname);
        user.setLname(this.lname);
        user.setRole(this.role);
        user.setPassword(this.password);
        user.setGender(this.gender);
        user.setPhone(this.phone);
        user.setStatus(this.status);

        return user;
    }
}
