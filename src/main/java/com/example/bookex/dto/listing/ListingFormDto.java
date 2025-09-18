package com.example.bookex.dto.listing;

import com.example.bookex.entity.enums.Condition;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingFormDto {

    private Long id;

    @NotBlank
    private String title;
    @NotBlank private String author;

    private String isbn;
    private Condition condition = Condition.GOOD;
    private boolean available = true;
    private String notes;
}
