package com.example.bookex.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingAverageDto {
    private double difficulty;
    private double emotion;
    private double enjoyment;
    private int count;
}

