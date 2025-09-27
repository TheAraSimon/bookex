package com.example.bookex.service;

import com.example.bookex.dto.book.BookDto;
import com.example.bookex.entity.Book;
import com.example.bookex.repository.BookRepository;
import com.example.bookex.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    @Transactional
    public Book findOrCreate(String title, String author, String isbn) {
        String normalizedTitle = normalize(title);
        String normalizedAuthor = normalize(author);
        return bookRepository.findByTitleAndAuthor(normalizedTitle, normalizedAuthor)
                .orElseGet(() -> {
                    Book b = Book.builder()
                            .title(normalizedTitle)
                            .author(normalizedAuthor)
                            .isbn(normalizeOrNull(isbn))
                            .build();
                    // @PrePersist sets timestamps
                    return bookRepository.save(b);
                });
    }

    public BookDto toDto(Book b) { return DtoMapper.toBookDto(b); }

    private static String normalize(String string) {
        if (string == null) throw new IllegalArgumentException("Value required");
        string = string.trim().toLowerCase();
        if (string.isEmpty()) throw new IllegalArgumentException("Value required");
        return string;
    }
    private static String normalizeOrNull(String string) {
        return (string == null || string.isBlank()) ? null : string.trim();
    }
}

