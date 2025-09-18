package com.example.bookex.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
