package com.example.bookex.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Max images per listing (FR-4)
     */
    @Min(1)
    @Max(5)
    private int maxImagesPerListing = 5;

    /**
     * Directory where we store uploaded files
     */
    @NotBlank
    private String uploadDir = "uploads";

    // --- Optional admin bootstrap ---
    private Admin admin = new Admin();

    @Getter
    @Setter
    public static class Admin {
        private String username = "admin";
        private String password = "admin123";
    }
}

