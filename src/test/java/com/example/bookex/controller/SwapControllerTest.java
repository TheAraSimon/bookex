package com.example.bookex.controller;

import com.example.bookex.entity.User;
import com.example.bookex.service.SwapService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SwapControllerTest {

    private User me() {
        return User.builder().id(5L).email("s@e.com").username("sam").password("x").build();
    }

    @Test
    void page_addsInboxOutboxAndOk_returnsSwaps() {
        SwapService swapService = mock(SwapService.class);
        SwapController swapController = new SwapController(swapService);

        Model model = new ExtendedModelMap();
        String view = swapController.page(me(), model, "OK");

        assertThat(view).isEqualTo("swaps");
        assertThat(model.containsAttribute("inbox")).isTrue();
        assertThat(model.containsAttribute("outbox")).isTrue();
        assertThat(model.getAttribute("ok")).isEqualTo("OK");

        verify(swapService).inbox(any(User.class));
        verify(swapService).outbox(any(User.class));
    }

    @Test
    void create_redirectsOk() {
        SwapService swapService = mock(SwapService.class);
        SwapController swapController = new SwapController(swapService);

        SwapController.CreateSwapForm swapForm = new SwapController.CreateSwapForm();
        swapForm.setListingId(10L);
        swapForm.setMessage("Hi");

        String view = swapController.create(me(), swapForm);
        assertThat(view).isEqualTo("redirect:/swaps?ok=Swap+requested");

        verify(swapService).createRequest(any(User.class), eq(10L), eq("Hi"));
    }

    @Test
    void accept_redirects() {
        SwapService swapService = mock(SwapService.class);
        SwapController swapController = new SwapController(swapService);

        String view = swapController.accept(me(), 9L);
        assertThat(view).isEqualTo("redirect:/swaps?ok=Accepted");
        verify(swapService).ownerRespond(any(User.class), eq(9L), eq(true));
    }

    @Test
    void decline_redirects() {
        SwapService swapServices = mock(SwapService.class);
        SwapController swapController = new SwapController(swapServices);

        String view = swapController.decline(me(), 9L);
        assertThat(view).isEqualTo("redirect:/swaps?ok=Declined");
        verify(swapServices).ownerRespond(any(User.class), eq(9L), eq(false));
    }

    @Test
    void complete_redirects() {
        SwapService swapServices = mock(SwapService.class);
        SwapController swapController = new SwapController(swapServices);

        String view = swapController.complete(me(), 9L);
        assertThat(view).isEqualTo("redirect:/swaps?ok=Completed");
        verify(swapServices).markCompleted(any(User.class), eq(9L));
    }

    @Test
    void cancel_redirects() {
        SwapService swapService = mock(SwapService.class);
        SwapController swapController = new SwapController(swapService);

        String view = swapController.cancel(me(), 9L);
        assertThat(view).isEqualTo("redirect:/swaps?ok=Cancelled");
        verify(swapService).markCancelled(any(User.class), eq(9L));
    }
}
