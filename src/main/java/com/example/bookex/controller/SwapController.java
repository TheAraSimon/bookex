package com.example.bookex.controller;

import com.example.bookex.entity.User;
import com.example.bookex.service.SwapService;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/swaps")
public class SwapController {

    private final SwapService swapService;

    @GetMapping
    public String page(@AuthenticationPrincipal User me, Model model,
                       @RequestParam(value="ok", required = false) String ok) {
        model.addAttribute("inbox", swapService.inbox(me));
        model.addAttribute("outbox", swapService.outbox(me));
        model.addAttribute("ok", ok);
        return "swaps";
    }

    @PostMapping("/create")
    public String create(@AuthenticationPrincipal User me, @ModelAttribute CreateSwapForm f) {
        swapService.createRequest(me, f.getListingId(), f.getMessage());
        return "redirect:/swaps?ok=Swap+requested";
    }

    @PostMapping("/{id}/accept")
    public String accept(@AuthenticationPrincipal User me, @PathVariable Long id) {
        swapService.ownerRespond(me, id, true);
        return "redirect:/swaps?ok=Accepted";
    }

    @PostMapping("/{id}/decline")
    public String decline(@AuthenticationPrincipal User me, @PathVariable Long id) {
        swapService.ownerRespond(me, id, false);
        return "redirect:/swaps?ok=Declined";
    }

    @PostMapping("/{id}/complete")
    public String complete(@AuthenticationPrincipal User me, @PathVariable Long id) {
        swapService.markCompleted(me, id);
        return "redirect:/swaps?ok=Completed";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@AuthenticationPrincipal User me, @PathVariable Long id) {
        swapService.markCancelled(me, id);
        return "redirect:/swaps?ok=Cancelled";
    }

    @Data
    public static class CreateSwapForm {
        private Long listingId;
        @Size(max = 300)
        private String message;
    }
}

