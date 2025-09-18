package com.example.bookex.controller;

import com.example.bookex.dto.auth.RegisterRequest;
import com.example.bookex.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService users;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("req")) {
            model.addAttribute("req", new RegisterRequest());
        }
        return "register";
    }

    // POST /register — обработать регистрацию
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("req") RegisterRequest req,
                                      BindingResult br,
                                      Model model,
                                      RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "register";
        }
        try {
            users.register(req.getEmail(), req.getUsername(), req.getPassword());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
        ra.addAttribute("ok", "Account created");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

}

