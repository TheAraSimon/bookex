package com.example.bookex.service;

import com.example.bookex.dto.rating.RatingAverageDto;
import com.example.bookex.entity.Book;
import com.example.bookex.entity.Rating;
import com.example.bookex.entity.RatingId;
import com.example.bookex.entity.User;
import com.example.bookex.exceptions.NotFoundException;
import com.example.bookex.repository.BookRepository;
import com.example.bookex.repository.RatingRepository;
import com.example.bookex.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;

    @Transactional
    public RatingAverageDto rate(User rater, Long bookId, int difficulty, int emotion, int enjoyment) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        validate1to5(difficulty, "difficulty");
        validate1to5(emotion, "emotion");
        validate1to5(enjoyment, "enjoyment");

        Rating rating = ratingRepository.findByUserAndBook(rater, book)
                .orElseGet(() -> Rating.builder()
                        .id(new RatingId(rater.getId(), book.getId()))
                        .user(rater)
                        .book(book)
                        .build()
                );

        rating.setDifficulty((short) difficulty);
        rating.setEmotion((short) emotion);
        rating.setEnjoyment((short) enjoyment);
        ratingRepository.save(rating);

        return computeAverages(book);
    }

    public RatingAverageDto getAverages(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));
        return computeAverages(book);
    }

    private RatingAverageDto computeAverages(Book book) {
        List<Rating> list = ratingRepository.findByBook(book);
        if (list.isEmpty()) return DtoMapper.toAvgDto(0, 0, 0, 0);

        double d = list.stream().mapToInt(Rating::getDifficulty).average().orElse(0);
        double e = list.stream().mapToInt(Rating::getEmotion).average().orElse(0);
        double j = list.stream().mapToInt(Rating::getEnjoyment).average().orElse(0);

        return DtoMapper.toAvgDto(round1(d), round1(e), round1(j), list.size());
    }

    private static double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static void validate1to5(int valuation, String name) {
        if (valuation < 1 || valuation > 5) {
            throw new IllegalArgumentException(name + " must be 1..5");
        }
    }
}
