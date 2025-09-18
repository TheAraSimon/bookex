package com.example.bookex.dto.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;      // HTTP reason (e.g., "Bad Request")
    private String message;    // safe, human-readable
    private String path;       // request path
    private String requestId;  // correlation id (X-Request-ID)
    private List<FieldErrorEntry> fieldErrors; // optional validation details
}
