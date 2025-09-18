package com.example.bookex.repository;

import com.example.bookex.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitleAndAuthor(String t, String a);
}
