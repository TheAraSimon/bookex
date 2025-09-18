package com.example.bookex.dto.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldErrorEntry {
    private String field;
    private String message;
}
