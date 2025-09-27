package com.example.bookex.controller;

import com.example.bookex.dto.auth.RegisterRequest;
import com.example.bookex.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Test
    void showRegistrationForm_addsEmptyReqAndReturnsView() {
        UserService userService = mock(UserService.class);
        AuthController authController = new AuthController(userService);

        Model model = new ExtendedModelMap();
        String view = authController.showRegistrationForm(model);

        assertThat(view).isEqualTo("register");
        assertThat(model.containsAttribute("req")).isTrue();
        assertThat(model.getAttribute("req")).isInstanceOf(RegisterRequest.class);
    }

    @Test
    void processRegistration_success_redirectsToLogin() {
        UserService userService = mock(UserService.class);
        AuthController authController = new AuthController(userService);

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("a@b.c")
                .username("alice")
                .password("secret123")
                .build();
        BindingResult bindingResult = new BeanPropertyBindingResult(registerRequest, "req");
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = authController.processRegistration(registerRequest, bindingResult, model, redirectAttributes);

        verify(userService).register("a@b.c", "alice", "secret123");
        assertThat(view).isEqualTo("redirect:/login");
    }

    @Test
    void processRegistration_serviceThrows_showsRegisterWithError() {
        UserService userService = mock(UserService.class);
        doThrow(new IllegalArgumentException("email taken"))
                .when(userService).register(any(), any(), any());
        AuthController authController = new AuthController(userService);

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("a@b.c")
                .username("alice")
                .password("secret123")
                .build();
        BindingResult bindingResult = new BeanPropertyBindingResult(registerRequest, "req");
        Model model = new ExtendedModelMap();
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = authController.processRegistration(registerRequest, bindingResult, model, redirectAttributes);

        assertThat(view).isEqualTo("register");
        assertThat(model.containsAttribute("error")).isTrue();
        assertThat(model.getAttribute("error")).isEqualTo("email taken");
    }

    @Test
    void showLoginForm_returnsLoginView() {
        UserService userService = mock(UserService.class);
        AuthController authController = new AuthController(userService);

        String view = authController.showLoginForm();
        assertThat(view).isEqualTo("login");
    }
}
