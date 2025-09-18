package com.example.bookex.dto.listing;

import com.example.bookex.dto.book.BookDto;
import com.example.bookex.dto.user.UserPublicDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingCardDto {
    private Long id;
    private BookDto book;
    private UserPublicDto owner;
    private String condition;
    private boolean available;
}

