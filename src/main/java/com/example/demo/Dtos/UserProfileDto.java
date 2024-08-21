package com.example.demo.Dtos;

import com.example.demo.Enums.Gender;
import com.example.demo.Enums.Role;
import com.example.demo.Enums.Status;
import com.example.demo.Models.User;
import com.example.demo.Repos.UserRepo;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@RequiredArgsConstructor
public class UserProfileDto
{
    @Size(min=3,max=25)
    private String fname;

    @Size(min=3,max=25)
    private String lname;

    @Size(min = 8,max = 15)
    private String password;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?!.*\\s).{8,}$",
            message = "Password must be atleast 8 characters long and contain " +
                    "at least one uppercase letter, one lowercase letter, " +
                    "one digit, and one special character.")
    private String new_password;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Email(message = "Enter a valid email, eg: example6_@gmail.com",
            regexp = "[a-z]{2,30}@[a-z]{2,10}\\.[a-z]{2,10}")
    private String email;

    @Size(min=10,max=10)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private String message;

    private boolean passwordValidated = false;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final UserRepo userRepo;

    public User updateUser (User user, UserRepo repo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager)
    {
        if(this.lname == null && this.lname == null &&
                this.gender == null && this.email == null &&
                this.phone == null && this.new_password == null) {
            this.message = "There is nothing to update";
            return null;
        }
        if(this.email != null || this.phone != null || this.new_password != null) {
            if(this.password == null) {
                this.message = "Provide a valid password to update " +
                        "sensitive information like phone number, email, password";
                return null;
            }

            if(this.email != null) {
                boolean emailExists = repo.existsByEmail(this.email);
                if(emailExists) {
                    this.message = "Email exists";
                    return null;
                }
            }
            if(this.phone != null) {
                boolean phoneExists = repo.existsByPhone(this.phone);
                if(phoneExists) {
                    this.message = "Phone number exists";
                    return null;
                }
            }
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    this.password,
                    user.getAuthorities()
            );

            if(!authenticationManager.authenticate(token).isAuthenticated()) {
                this.message = "Incorrect password";
                return null;
            }
        }

        if(this.fname != null) {
            user.setFname(this.fname);
        }
        if(this.lname != null) {
            user.setLname(this.lname);
        }
        if(this.gender != null) {
            user.setGender(this.gender);
        }
        if(this.email != null) {
            user.setEmail(this.email);
        }
        if(this.phone != null) {
            user.setPhone(this.phone);
        }

        if(this.new_password != null) {
            user.setPassword(passwordEncoder.encode(this.new_password));
        }

        user = repo.save(user);
        return user;
    }
}
