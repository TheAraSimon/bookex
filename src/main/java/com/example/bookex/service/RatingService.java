package com.example.bookex.service;

import com.example.bookex.dto.rating.RatingAverageDto;
import com.example.bookex.entity.Book;
import com.example.bookex.entity.Rating;
import com.example.bookex.entity.RatingId;
import com.example.bookex.entity.User;
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

    private final RatingRepository ratings;
    private final BookRepository books;

    @Transactional
    public RatingAverageDto rate(User rater, Long bookId, int difficulty, int emotion, int enjoyment) {
        Book book = books.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if (book == null) throw new IllegalArgumentException("Book not found");

        validate1to5(difficulty, "difficulty");
        validate1to5(emotion, "emotion");
        validate1to5(enjoyment, "enjoyment");

        Rating rating = ratings.findByUserAndBook(rater, book)
                .orElseGet(() -> Rating.builder()
                        .id(new RatingId(rater.getId(), book.getId()))
                        .user(rater)
                        .book(book)
                        .build()
                );

        rating.setDifficulty((short) difficulty);
        rating.setEmotion((short) emotion);
        rating.setEnjoyment((short) enjoyment);
        ratings.save(rating);

        return computeAverages(book);
    }

    public RatingAverageDto getAverages(Long bookId) {
        Book b = books.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        return computeAverages(b);
    }

    private RatingAverageDto computeAverages(Book book) {
        List<Rating> list = ratings.findByBook(book);
        if (list.isEmpty()) return DtoMapper.toAvgDto(0, 0, 0, 0);

        double d = list.stream().mapToInt(r -> r.getDifficulty()).average().orElse(0);
        double e = list.stream().mapToInt(r -> r.getEmotion()).average().orElse(0);
        double j = list.stream().mapToInt(r -> r.getEnjoyment()).average().orElse(0);

        return DtoMapper.toAvgDto(round1(d), round1(e), round1(j), list.size());
    }

    private static double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static void validate1to5(int v, String name) {
        if (v < 1 || v > 5) throw new IllegalArgumentException(name + " must be 1..5");
    }
}

