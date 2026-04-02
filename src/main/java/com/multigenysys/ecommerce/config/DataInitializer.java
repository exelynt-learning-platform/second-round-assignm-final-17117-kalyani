package com.multigenysys.ecommerce.config;

import com.multigenysys.ecommerce.entity.Role;
import com.multigenysys.ecommerce.entity.User;
import com.multigenysys.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultAdminEmail;
    private final String defaultAdminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           @Value("${app.default-admin-email:admin@shop.com}") String defaultAdminEmail,
                           @Value("${app.default-admin-password:}") String defaultAdminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultAdminEmail = defaultAdminEmail;
        this.defaultAdminPassword = defaultAdminPassword;
    }

    @Override
    public void run(String... args) {
        if (defaultAdminPassword == null || defaultAdminPassword.isBlank()) {
            return;
        }
        if (!userRepository.existsByEmail(defaultAdminEmail)) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(defaultAdminEmail);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
        }
    }
}
