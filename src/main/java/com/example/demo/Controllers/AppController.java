package com.example.demo.Controllers;

import com.example.demo.Dtos.*;
import com.example.demo.Dtos.ResponseDtos.LoginResponseDto;
import com.example.demo.Dtos.ResponseDtos.UserResponseDto;
import com.example.demo.Enums.Status;
import com.example.demo.Models.User;
import com.example.demo.Repos.UserRepo;
import com.example.demo.Services.EmailService;
import com.example.demo.Services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.*;

@Setter
@Getter
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AppController
{
    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UserRepo userRepo;

    @PostMapping("/home")
    public ResponseEntity<?> mean ()
    {
        return ResponseEntity.ok("Welcome to Thrift_api, whats up?");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto user)
    {
        try {
            Optional<User> chk_chk = userRepo.findByEmail( user.getEmail());

            if(chk_chk.isPresent() || userRepo.existsByPhone(user.getPhone()))
            {
                return new ResponseEntity<>("user already exists", HttpStatus.BAD_REQUEST);
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User saved = userRepo.save(user.getUser());

            emailService
                    .sendEmail(saved.getEmail(), "Welcome to our platform", "You have succesfully  registered on our platform");
            UserResponseDto resDto = new UserResponseDto(saved);
            return ResponseEntity.ok(resDto);
        } catch (Exception e) {
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto login)
    {
        try {
            Optional<User> chk_chk = userRepo.findByEmail(login.getEmail());

            if(chk_chk.isEmpty())
            {
                return new ResponseEntity<>("email or password incorrect", HttpStatus.BAD_REQUEST);
            }

            User user = chk_chk.get();
            if(user.getStatus().equals(Status.INACTIVE) || user.getStatus().equals(Status.DELETED)) {
                if(user.getStatus().equals(Status.INACTIVE)) {
                    return new ResponseEntity<>("Your account have been deactivated, go to forget password to reactivate", HttpStatus.BAD_REQUEST);
                }
                return new ResponseEntity<>("Your account have been deleted", HttpStatus.BAD_REQUEST);
            }
            UsernamePasswordAuthenticationToken token =new UsernamePasswordAuthenticationToken(
                    login.getEmail(),
                    login.getPassword(),
                    user.getAuthorities()
            );

            if(!authenticationManager.authenticate(token).isAuthenticated()) {
                return new ResponseEntity<>("email or password incorrect", HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> extraClaims = setExtraClaims(user);
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", jwtService.generateJwt(user,extraClaims, new Date(System.currentTimeMillis() + 1000*60*60)));
            tokens.put("refreshToken", jwtService.generateJwt(user));

            user.setRefreshToken(tokens.get("refreshToken"));
            userRepo.save(user);
            LoginResponseDto resDto = new LoginResponseDto(user, tokens);

            return ResponseEntity.ok(resDto);
        } catch(BadCredentialsException e) {
            return new ResponseEntity<>("email or password incorrect", HttpStatus.BAD_REQUEST);
        } catch(Exception e) {
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenDto dto, HttpServletRequest req)
    {
        try {
            String jwt = dto.getToken();
            User user = jwtService.getUser(jwt);
            if(user.getId() != dto.getUserId()) {
                return new ResponseEntity<>("User does not exist", HttpStatus.BAD_REQUEST);
            }
            if(!jwtService.validateToken(user, jwt) || jwtService.blacklisted(jwt)) {
                return new ResponseEntity<>("Invalid token, user should login again", HttpStatus.BAD_REQUEST);
            }
            Map<String, String> res = new HashMap<>();
            Map<String, Object> extraClaims = setExtraClaims(user);
            res.put("accessToken", jwtService.generateJwt(user,extraClaims, new Date(System.currentTimeMillis() + 1000*60*60)));
            res.put("message", "token generated succesfully");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@Valid @RequestBody ForgetPasswordDto dto)
    {
        try {
            Optional<User> chk_chk = userRepo.findByEmail(dto.getEmail());

            if(chk_chk.isEmpty())
            {
                return new ResponseEntity<>("email or password incorrect", HttpStatus.BAD_REQUEST);
            }
            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);
            User user = chk_chk.get();
            user.setToken(token);
            user.setTokenExpiryTime(expiry);
            userRepo.save(user);
            String link = dto.getFrontUrl() + "/" + token;
            emailService
                    .sendEmail(user.getEmail(), "Click the link below to update your password", link);

            return ResponseEntity.ok("Password reset link succesfully sent to "+user.getEmail());
        } catch (Exception e) {
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forget-password/update")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordDto dto)
    {
        try {
            Optional<User> chk = userRepo.findByToken(dto.getToken());
            if(chk.isEmpty()) {
                return new ResponseEntity<>("Invalid token", HttpStatus.BAD_REQUEST);
            }
            User user = chk.get();
            if(user.getTokenExpiryTime().isBefore(LocalDateTime.now())) {
                return new ResponseEntity<>("Token is expired", HttpStatus.BAD_REQUEST);
            }

            user.setPassword(passwordEncoder.encode(dto.getNew_password()));

            userRepo.save(user);

            return ResponseEntity.ok("Password updated succesfully");
        } catch(Exception e) {
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static Map<String, Object> setExtraClaims(User user)
    {
        Map <String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id",user.getLname());
        extraClaims.put("role",user.getEmail());
        extraClaims.put("status",user.getEmail());

        return extraClaims;
    }
}
