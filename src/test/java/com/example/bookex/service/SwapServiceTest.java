package com.example.bookex.service;

import com.example.bookex.entity.Book;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.SwapRequest;
import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.Condition;
import com.example.bookex.entity.enums.SwapStatus;
import com.example.bookex.exceptions.NotFoundException;
import com.example.bookex.repository.BookListingRepository;
import com.example.bookex.repository.SwapRequestRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SwapServiceTest {

    @Test
    void createRequest_succeeds_forAvailableListing_andDifferentUser() {
        SwapRequestRepository swapRequestRepository = mock(SwapRequestRepository.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        SwapService swapService = new SwapService(swapRequestRepository, bookListingRepository);

        User owner = User.builder().id(10L).build();
        Book book = Book.builder().id(200L).title("dune").author("frank herbert").build();

        BookListing bookListing = BookListing.builder()
                .id(100L)
                .user(owner)
                .book(book)
                .condition(Condition.GOOD)
                .available(true)
                .build();

        when(bookListingRepository.findById(100L)).thenReturn(Optional.of(bookListing));
        when(swapRequestRepository.save(any(SwapRequest.class))).thenAnswer(inv -> {
            SwapRequest swapRequest = inv.getArgument(0);
            swapRequest.setId(555L);
            return swapRequest;
        });

        User requester = User.builder().id(20L).build();
        swapService.createRequest(requester, 100L, "hi!");

        verify(swapRequestRepository).save(argThat(swapRequest ->
                swapRequest.getListing().getId().equals(100L) &&
                        swapRequest.getUser().getId().equals(20L) &&
                        swapRequest.getStatus() == SwapStatus.PENDING));
    }

    @Test
    void createRequest_throws_whenListingNotFound() {
        SwapService swapService = new SwapService(mock(SwapRequestRepository.class), mock(BookListingRepository.class));
        assertThatThrownBy(() -> swapService.createRequest(User.builder().id(1L).build(), 999L, "x"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createRequest_throws_whenOwnListing() {
        SwapRequestRepository swapRequestRepository = mock(SwapRequestRepository.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        SwapService swapService = new SwapService(swapRequestRepository, bookListingRepository);

        User owner = User.builder().id(7L).build();
        BookListing bookListing = BookListing.builder()
                .id(1L)
                .user(owner)
                .book(Book.builder().id(300L).title("t").author("a").build())
                .condition(Condition.GOOD)   // для консистентности
                .available(true)
                .build();

        when(bookListingRepository.findById(1L)).thenReturn(Optional.of(bookListing));

        assertThatThrownBy(() -> swapService.createRequest(owner, 1L, "self"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createRequest_throws_whenListingNotAvailable() {
        SwapRequestRepository swapRequestRepository = mock(SwapRequestRepository.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        SwapService swapService = new SwapService(swapRequestRepository, bookListingRepository);

        User owner = User.builder().id(7L).build();
        BookListing bookListing = BookListing.builder()
                .id(1L)
                .user(owner)
                .book(Book.builder().id(400L).title("t").author("a").build())
                .condition(Condition.GOOD)   // для консистентности
                .available(false)
                .build();

        when(bookListingRepository.findById(1L)).thenReturn(Optional.of(bookListing));

        assertThatThrownBy(() -> swapService.createRequest(User.builder().id(8L).build(), 1L, "hey"))
                .isInstanceOf(IllegalStateException.class);
    }
}
