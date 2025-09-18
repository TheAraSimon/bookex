package com.example.bookex.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPublicDto {
    private Long id;
    private String displayName;

    // can be null if not public
    private String contact; // email or phone
}
