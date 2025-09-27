package com.example.bookex.controller;

import com.example.bookex.dto.user.UserProfileDto;
import com.example.bookex.entity.User;
import com.example.bookex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String profile(@AuthenticationPrincipal User me, Model model,
                          @RequestParam(value="ok", required = false) String ok) {
        model.addAttribute("profile", userService.getProfile(me.getId()));
        model.addAttribute("ok", ok);
        return "profile";
    }

    @PostMapping
    public String save(@AuthenticationPrincipal User me,
                       @Valid @ModelAttribute("profile") UserProfileDto dto,
                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "profile";
        userService.updateProfile(me.getId(), dto);
        return "redirect:/profile?ok=Profile+saved";
    }
}

