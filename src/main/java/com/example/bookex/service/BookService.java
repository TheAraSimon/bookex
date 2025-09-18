package com.example.bookex.service;

import com.example.bookex.dto.book.BookDto;
import com.example.bookex.entity.Book;
import com.example.bookex.repository.BookRepository;
import com.example.bookex.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository books;

    @Transactional
    public Book findOrCreate(String title, String author, String isbn) {
        String t = normalize(title);
        String a = normalize(author);
        return books.findByTitleAndAuthor(t, a)
                .orElseGet(() -> {
                    Book b = Book.builder()
                            .title(t)
                            .author(a)
                            .isbn(normalizeOrNull(isbn))
                            .build();
                    // @PrePersist sets timestamps
                    return books.save(b);
                });
    }

    public BookDto toDto(Book b) { return DtoMapper.toBookDto(b); }

    private static String normalize(String s) {
        if (s == null) throw new IllegalArgumentException("Value required");
        s = s.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Value required");
        return s;
    }
    private static String normalizeOrNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

