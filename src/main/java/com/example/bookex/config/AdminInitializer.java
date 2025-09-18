package com.example.bookex.config;

import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.Role;
import com.example.bookex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final AppProperties props;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initAdmin() {
        String username = props.getAdmin().getUsername();
        String password = props.getAdmin().getPassword();
        if (username == null || password == null) return;

        users.findByUsername(username).ifPresentOrElse(
                u -> log.info("Admin '{}' already present", username),
                () -> {
                    User admin = User.builder()
                            .username(username)
                            .email(username + "@local")
                            .password(encoder.encode(password))
                            .role(Role.ADMIN)
                            .build();
                    users.save(admin);
                    log.info("Created admin '{}'", username);
                }
        );
    }
}
