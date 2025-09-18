package com.example.bookex.repository;

import com.example.bookex.entity.SwapRequest;
import com.example.bookex.entity.User;
import com.example.bookex.entity.BookListing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SwapRequestRepository extends JpaRepository<SwapRequest, Long> {
    List<SwapRequest> findByListingInOrderByCreatedAtDesc(List<BookListing> listings);
    List<SwapRequest> findByUserOrderByCreatedAtDesc(User user);
}

