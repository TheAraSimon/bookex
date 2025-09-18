package com.example.bookex.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {
    @EmbeddedId
    private RatingId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rating_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rating_book"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    @Column(nullable = false)
    @Min(1) @Max(5)
    private short difficulty;

    @Column(nullable = false)
    @Min(1) @Max(5)
    private short emotion;

    @Column(nullable = false)
    @Min(1) @Max(5)
    private short enjoyment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

