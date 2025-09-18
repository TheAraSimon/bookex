package com.example.bookex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookImageId implements Serializable {
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "image_no", nullable = false)
    @Min(1) @Max(5)
    private short imageNo; // SMALLINT (1..5)
}
