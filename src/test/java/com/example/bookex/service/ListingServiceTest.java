package com.example.bookex.service;

import com.example.bookex.dto.listing.ListingFormDto;
import com.example.bookex.entity.Book;
import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.User;
import com.example.bookex.entity.enums.Condition;
import com.example.bookex.exceptions.NotFoundException;
import com.example.bookex.repository.BookImageRepository;
import com.example.bookex.repository.BookListingRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ListingServiceTest {

    @Test
    void createListing_savesListing_usingBookService() {
        BookService bookService = mock(BookService.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        BookImageRepository bookImageRepository = mock(BookImageRepository.class);

        ListingService listingService = new ListingService(bookService, bookListingRepository, bookImageRepository);

        User owner = User.builder().id(10L).build();
        Book book = Book.builder().id(7L).title("dune").author("frank herbert").build();
        when(bookService.findOrCreate("Dune", "Frank Herbert", ""))
                .thenReturn(book);
        when(bookListingRepository.save(any(BookListing.class))).thenAnswer(inv -> {
            BookListing l = inv.getArgument(0);
            l.setId(100L);
            return l;
        });

        ListingFormDto listingFormDto = ListingFormDto.builder()
                .title("Dune")
                .author("Frank Herbert")
                .isbn("")
                .condition(Condition.GOOD)
                .available(true)
                .notes("Nice copy")
                .build();

        listingService.createListing(owner, listingFormDto);

        verify(bookService).findOrCreate("Dune", "Frank Herbert", "");
        verify(bookListingRepository).save(any(BookListing.class));
        verify(bookImageRepository).findByListingOrderByIdImageNoAsc(any());
    }

    @Test
    void updateListing_throws_whenNotFound() {
        BookService bookService = mock(BookService.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        BookImageRepository bookImageRepository = mock(BookImageRepository.class);

        ListingService listingService = new ListingService(bookService, bookListingRepository, bookImageRepository);
        when(bookListingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.updateListing(
                User.builder().id(1L).build(), 999L,
                ListingFormDto.builder().title("t").author("a").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteListing_throws_whenNotFound() {
        BookService bookService = mock(BookService.class);
        BookListingRepository bookListingRepository = mock(BookListingRepository.class);
        BookImageRepository bookImageRepository = mock(BookImageRepository.class);

        ListingService listingService = new ListingService(bookService, bookListingRepository, bookImageRepository);
        when(bookListingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listingService.deleteListing(User.builder().id(1L).build(), 99L))
                .isInstanceOf(NotFoundException.class);
    }
}
