package com.example.bookex.service;

import com.example.bookex.dto.user.UserProfileDto;
import com.example.bookex.dto.user.UserPublicDto;
import com.example.bookex.entity.User;
import com.example.bookex.repository.UserRepository;
import com.example.bookex.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Transactional
    public User register(String email, String displayName, String rawPassword) {
        email = email.trim().toLowerCase();
        displayName = displayName.trim();

        if (users.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (users.existsByUsername(displayName)) {
            throw new IllegalArgumentException("Username already taken");
        }
        User u = new User();
        u.setEmail(email);
        u.setUsername(displayName);
        u.setPassword(encoder.encode(rawPassword));
        return users.save(u);
    }

    public Optional<User> findByEmail(String email) { return users.findByEmail(email); }
    public Optional<User> findById(Long id) { return users.findById(id); }

    public UserProfileDto getProfile(Long userId) {
        User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return DtoMapper.toProfileDto(u);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UserProfileDto dto) {
        User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        DtoMapper.updateUserFromProfile(dto, u);
        users.save(u);
        return DtoMapper.toProfileDto(u);
    }

    public UserPublicDto toPublic(User u, boolean reveal) {
        return DtoMapper.toUserPublic(u, reveal);
    }
}
