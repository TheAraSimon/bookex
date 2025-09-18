package com.example.bookex.repository;

import com.example.bookex.entity.BookListing;
import com.example.bookex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BookListingRepository extends JpaRepository<BookListing, Long> {
    List<BookListing> findByAvailableTrue();
    List<BookListing> findByUser(User owner);
}

