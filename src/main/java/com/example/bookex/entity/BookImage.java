package com.example.bookex.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "book_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookImage {
    @EmbeddedId
    private BookImageId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("listingId")
    @JoinColumn(name = "listing_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_book_image_listing"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BookListing listing;

    @Column(nullable = false, length = 500)
    private String path; // served under /uploads/**

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
