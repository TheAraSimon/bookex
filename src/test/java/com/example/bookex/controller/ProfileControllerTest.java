package com.example.bookex.controller;

import com.example.bookex.dto.user.UserProfileDto;
import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.ContactMethod;
import com.example.bookex.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    private User me() {
        return User.builder().id(77L).email("p@e.com").username("pro").password("x").build();
    }

    @Test
    void profile_addsDtoAndOk_returnsProfile() {
        UserService userService = mock(UserService.class);
        ProfileController profileController = new ProfileController(userService);

        when(userService.getProfile(77L)).thenReturn(UserProfileDto.builder()
                .displayName("Alex")
                .publicContact(true)
                .preferredMethod(ContactMethod.EMAIL)
                .contactEmail("a@b.c")
                .contactPhone("+1555")
                .build());

        Model model = new ExtendedModelMap();
        String view = profileController.profile(me(), model, "OK");

        assertThat(view).isEqualTo("profile");
        assertThat(model.containsAttribute("profile")).isTrue();
        assertThat(model.getAttribute("ok")).isEqualTo("OK");
    }

    @Test
    void save_valid_returnsRedirect() {
        UserService userService = mock(UserService.class);
        ProfileController profileController = new ProfileController(userService);

        UserProfileDto userProfileDto = UserProfileDto.builder()
                .displayName("Alex")
                .publicContact(true)
                .preferredMethod(ContactMethod.EMAIL)
                .contactEmail("a@b.c")
                .contactPhone("+1555").build();

        BindingResult bindingResult = new BeanPropertyBindingResult(userProfileDto, "profile");
        String view = profileController.save(me(), userProfileDto, bindingResult);

        assertThat(view).isEqualTo("redirect:/profile?ok=Profile+saved");
        verify(userService).updateProfile(eq(77L), any(UserProfileDto.class));
    }

    @Test
    void save_withBindingErrors_returnsProfileView() {
        UserService userService = mock(UserService.class);
        ProfileController profileController = new ProfileController(userService);

        UserProfileDto userProfileDto = new UserProfileDto();
        BindingResult bindingResult = new BeanPropertyBindingResult(userProfileDto, "profile");
        bindingResult.rejectValue("displayName", "NotBlank", "required");

        String view = profileController.save(me(), userProfileDto, bindingResult);

        assertThat(view).isEqualTo("profile");
        verify(userService, never()).updateProfile(anyLong(), any());
    }
}
