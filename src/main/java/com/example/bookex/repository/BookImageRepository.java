package com.example.bookex.repository;

import com.example.bookex.entity.BookImage;
import com.example.bookex.entity.BookImageId;
import com.example.bookex.entity.BookListing;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BookImageRepository extends JpaRepository<BookImage, BookImageId> {
    List<BookImage> findByListingOrderByIdImageNoAsc(BookListing listing);
    long countByListing(BookListing listing);
}

