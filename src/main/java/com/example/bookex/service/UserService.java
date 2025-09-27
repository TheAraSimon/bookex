package com.example.bookex.service;

import com.example.bookex.dto.user.UserProfileDto;
import com.example.bookex.dto.user.UserPublicDto;
import com.example.bookex.entity.User;
import com.example.bookex.exceptions.NotFoundException;
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

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public User register(String email, String displayName, String rawPassword) {
        email = email.trim().toLowerCase();
        displayName = displayName.trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.existsByUsername(displayName)) {
            throw new IllegalArgumentException("Username already taken");
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(displayName);
        user.setPassword(encoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }
    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        return DtoMapper.toProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UserProfileDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        DtoMapper.updateUserFromProfile(dto, user);
        userRepository.save(user);
        return DtoMapper.toProfileDto(user);
    }

    public UserPublicDto toPublic(User user, boolean reveal) {
        return DtoMapper.toUserPublic(user, reveal);
    }
}
