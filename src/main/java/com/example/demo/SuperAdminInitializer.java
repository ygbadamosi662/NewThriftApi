package com.example.demo;

import com.example.demo.Enums.Gender;
import com.example.demo.Enums.Status;
import com.example.demo.Models.User;
import com.example.demo.Enums.Role;
import com.example.demo.Repos.UserRepo;
import com.example.demo.Services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperAdminInitializer implements ApplicationRunner {

    @Value("${app.user.email}")
    private String email;

    @Value("${app.user.fname}")
    private String fname;

    @Value("${app.user.lname}")
    private String lname;

    @Value("${app.user.gender}")
    private String gender;

    @Value("${app.user.phone}")
    private String phone;

    @Value("${app.user.password}")
    private String password;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!userRepo.existsByRole(Role.SUPER_ADMIN)) {
            User superAdmin = new User();
            superAdmin.setStatus(Status.ACTIVE);
            superAdmin.setPassword(passwordEncoder.encode(this.password));
            superAdmin.setEmail(this.email);
            superAdmin.setFname(this.fname);
            superAdmin.setLname(this.lname);
            superAdmin.setGender(Gender.valueOf(this.gender));
            superAdmin.setRole(Role.SUPER_ADMIN);

            userRepo.save(superAdmin);

            emailService.sendEmail(superAdmin.getEmail(), "Super Admin set for the Phone Book Api",
                    "You have been set as the super admni of this platform, you have unlimited " +
                            "access to the resources of this platform");
        }
        System.out.println("Super Admin is set");
    }
}
