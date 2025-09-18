package com.example.bookex.dto.swap;

import com.example.bookex.dto.listing.ListingCardDto;
import com.example.bookex.dto.user.UserPublicDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwapViewDto {
    private Long id;
    private String status;
    private ListingCardDto listing;
    private UserPublicDto requester;
    private UserPublicDto owner;
}

