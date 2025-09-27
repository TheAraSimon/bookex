package com.example.bookex.service;

import com.example.bookex.dto.user.UserProfileDto;
import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.ContactMethod;
import com.example.bookex.entity.enums.Role;
import com.example.bookex.exceptions.NotFoundException;
import com.example.bookex.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void register_createsUser_whenEmailAndUsernameAreFree() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, encoder);

        String email = "newuser@example.com";
        String username = "newuser";
        String rawPass = "secret123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(encoder.encode(rawPass)).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        userService.register(email, username, rawPass);

        verify(userRepository).save(argThat(user ->
                email.equals(user.getEmail()) &&
                        username.equals(user.getUsername()) &&
                        "ENCODED".equals(user.getPassword()) &&
                        user.getRole() == Role.USER
        ));
    }

    @Test
    void register_throws_whenEmailAlreadyUsed() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, encoder);

        when(userRepository.findByEmail("dup@example.com"))
                .thenReturn(Optional.of(User.builder().id(10L).build()));

        assertThatThrownBy(() -> userService.register("dup@example.com", "whatever", "pass"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_throws_whenUsernameAlreadyUsed() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, encoder);

        when(userRepository.findByEmail("free@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("free@example.com", "taken", "pass"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getProfile_returnsDto_whenUserExists() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, encoder);

        User user = User.builder()
                .id(7L)
                .email("u@example.com")
                .username("u")
                .contactEmail("pub@example.com")
                .contactPhone("+15551234567")
                .publicContact(true)
                .preferredMethod(ContactMethod.EMAIL)
                .build();
        user.setUsername("userx");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        UserProfileDto userProfileDto = userService.getProfile(7L);

        assertThat(userProfileDto).isNotNull();

        assertThat(userProfileDto.getContactEmail()).isEqualTo("pub@example.com");
        assertThat(userProfileDto.getContactPhone()).isEqualTo("+15551234567");
        assertThat(userProfileDto.isPublicContact()).isTrue();
        assertThat(userProfileDto.getPreferredMethod()).isEqualTo(ContactMethod.EMAIL);
    }

    @Test
    void getProfile_throws_whenUserMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, encoder);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateProfile_updatesEntity_andSaves() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, encoder);

        User user = User.builder()
                .id(5L)
                .email("x@example.com")
                .username("user5")
                .publicContact(false)
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDto userProfileDto = UserProfileDto.builder()
                .displayName("New Name")
                .publicContact(true)
                .preferredMethod(ContactMethod.PHONE)
                .contactEmail("new@example.com")
                .contactPhone("+380501112233")
                .build();

        userService.updateProfile(5L, userProfileDto);

        verify(userRepository).save(argThat(u ->
                u.isPublicContact() &&
                        u.getPreferredMethod() == ContactMethod.PHONE &&
                        "new@example.com".equals(u.getContactEmail()) &&
                        "+380501112233".equals(u.getContactPhone())
        ));
    }

    @Test
    void updateProfile_throws_whenUserMissing() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userRepository, encoder);

        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(404L, UserProfileDto.builder().build()))
                .isInstanceOf(NotFoundException.class);
    }
}
