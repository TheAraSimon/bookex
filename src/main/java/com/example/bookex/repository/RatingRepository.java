package com.example.bookex.repository;

import com.example.bookex.entity.Rating;
import com.example.bookex.entity.Book;
import com.example.bookex.entity.RatingId;
import com.example.bookex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface RatingRepository extends JpaRepository<Rating, RatingId> {
    Optional<Rating> findByUserAndBook(User user, Book book);
    List<Rating> findByBook(Book book);
}

