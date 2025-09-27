package com.example.bookex.service;

import com.example.bookex.entity.Book;
import com.example.bookex.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Test
    void findOrCreate_createsNew_whenNotExists_andNormalizes() {
        BookRepository bookRepository = mock(BookRepository.class);
        when(bookRepository.findByTitleAndAuthor("the hobbit", "j.r.r. tolkien")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book book = inv.getArgument(0);
            book.setId(1L);
            return book;
        });

        BookService bookService = new BookService(bookRepository);

        Book book = bookService.findOrCreate("  The Hobbit  ", "  J.R.R. Tolkien ", " 9780261103344 ");

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book saved = captor.getValue();

        assertThat(book.getId()).isEqualTo(1L);
        assertThat(saved.getTitle()).isEqualTo("the hobbit");
        assertThat(saved.getAuthor()).isEqualTo("j.r.r. tolkien");
        assertThat(saved.getIsbn()).isEqualTo("9780261103344");
    }

    @Test
    void findOrCreate_returnsExisting_whenFound() {
        BookRepository bookRepository = mock(BookRepository.class);
        Book existingBook = Book.builder().id(42L).title("dune").author("frank herbert").isbn("9780441172719").build();
        when(bookRepository.findByTitleAndAuthor("dune", "frank herbert")).thenReturn(Optional.of(existingBook));

        BookService bookService = new BookService(bookRepository);
        Book book = bookService.findOrCreate("Dune", "Frank Herbert", "9780441172719");

        assertThat(book.getId()).isEqualTo(42L);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void findOrCreate_allowsNullOrBlankIsbn() {
        BookRepository bookRepository = mock(BookRepository.class);
        when(bookRepository.findByTitleAndAuthor("1984", "george orwell")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookService bookService = new BookService(bookRepository);
        Book book = bookService.findOrCreate("1984", "George Orwell", "  ");

        assertThat(book.getIsbn()).isNull();
    }
}
