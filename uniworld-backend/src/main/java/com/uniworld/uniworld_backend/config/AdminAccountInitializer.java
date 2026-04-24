package com.uniworld.uniworld_backend.config;

import com.uniworld.uniworld_backend.User;
import com.uniworld.uniworld_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public AdminAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.admin.name:Admin}") String adminName
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            logger.info("Admin bootstrap skipped. Set app.admin.email and app.admin.password to enable it.");
            return;
        }

        User adminUser = userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User user = new User();
            user.setEmail(adminEmail);
            return user;
        });

        adminUser.setName(adminName);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setRole("ADMIN");

        userRepository.save(adminUser);
    }
}
