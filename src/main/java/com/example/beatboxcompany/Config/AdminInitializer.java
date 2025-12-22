package com.example.beatboxcompany.Config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.example.beatboxcompany.Entity.User;
import com.example.beatboxcompany.Repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    // .name("System Admin")
                    .email("admin@store.com")
                    .roles(List.of("ROLE_ADMIN"))
                    .verified(true)
                    .verificationToken(null)
                    .tokenExpiry(null)
                    // .address("Head Office")
                    // .phoneNumber("0000000000")
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin created: username=admin, password=admin123");
        } else {
            System.out.println("Admin account already exists.");
        }
    }
}
