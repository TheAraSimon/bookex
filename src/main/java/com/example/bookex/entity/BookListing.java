package com.example.bookex.entity;

import com.example.bookex.entity.enums.Condition;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(
        name = "book_listing",
        indexes = {
                @Index(name = "idx_book_listing_owner", columnList = "user_id"),
                @Index(name = "idx_book_listing_book", columnList = "book_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_book_listing_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_book_listing_book"))
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Book book;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Condition condition = Condition.GOOD;

    @Column(nullable = false)
    private boolean available = true;

    @Column(length = 500)
    private String notes;

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
