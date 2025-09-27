package com.example.bookex.service;

import com.example.bookex.dto.rating.RatingAverageDto;
import com.example.bookex.entity.Book;
import com.example.bookex.entity.Rating;
import com.example.bookex.entity.User;
import com.example.bookex.exceptions.NotFoundException;
import com.example.bookex.repository.BookRepository;
import com.example.bookex.repository.RatingRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RatingServiceTest {

    @Test
    void rate_rejects_outOfRangeValues() {
        RatingRepository ratingRepository = mock(RatingRepository.class);
        BookRepository bookRepository = mock(BookRepository.class);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(Book.builder().id(1L).build()));

        RatingService ratingService = new RatingService(ratingRepository, bookRepository);
        User user = User.builder().id(5L).build();

        assertThatThrownBy(() -> ratingService.rate(user, 1L, 0, 3, 4)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ratingService.rate(user, 1L, 1, 6, 4)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ratingService.rate(user, 1L, 1, 3, 9)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rate_throws_whenBookNotFound() {
        RatingService ratingService = new RatingService(mock(RatingRepository.class), mock(BookRepository.class));
        assertThatThrownBy(() -> ratingService.rate(User.builder().id(1L).build(), 999L, 3, 3, 3))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAverages_computesRoundedAverages() {
        RatingRepository ratingRepository = mock(RatingRepository.class);
        BookRepository bookRepository = mock(BookRepository.class);
        Book book = Book.builder().id(3L).build();
        when(bookRepository.findById(3L)).thenReturn(Optional.of(book));
        when(ratingRepository.findByBook(book)).thenReturn(List.of(
                Rating.builder().difficulty((short)3).emotion((short)4).enjoyment((short)5).build(),
                Rating.builder().difficulty((short)4).emotion((short)2).enjoyment((short)4).build()
        ));

        RatingService ratingService = new RatingService(ratingRepository, bookRepository);

        RatingAverageDto ratingAverageDto = ratingService.getAverages(3L);
        assertThat(ratingAverageDto.getDifficulty()).isEqualTo(3.5);
        assertThat(ratingAverageDto.getEmotion()).isEqualTo(3.0);
        assertThat(ratingAverageDto.getEnjoyment()).isEqualTo(4.5);
        assertThat(ratingAverageDto.getCount()).isEqualTo(2);
    }
}
