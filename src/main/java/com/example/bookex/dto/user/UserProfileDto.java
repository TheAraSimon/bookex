package com.example.bookex.dto.user;

import com.example.bookex.entity.enums.ContactMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    @NotBlank @Size(min=2, max=80)
    private String displayName;

    private boolean publicContact;

    private ContactMethod preferredMethod; // EMAIL | PHONE | null

    @Email @Size(max=255)
    private String contactEmail;

    @Pattern(regexp="^\\+?[0-9]{7,15}$", message="Phone must look like +15551234567")
    private String contactPhone;
}

