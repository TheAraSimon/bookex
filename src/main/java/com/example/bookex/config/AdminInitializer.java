package com.example.bookex.config;

import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.Role;
import com.example.bookex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        users.findByUsername(adminUsername).ifPresentOrElse(u ->
                        log.info("Admin '{}' already present", adminUsername),
                () -> {
                    User admin = User.builder()
                            .username(adminUsername)
                            .email(adminUsername + "@example.com")
                            .password(encoder.encode(adminPassword))
                            .role(Role.ADMIN)
                            .build();
                    users.save(admin);
                    log.info("Admin '{}' created", adminUsername);
                }
        );
    }
}
