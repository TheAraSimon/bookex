package com.example.bookex.exceptions;

import com.example.bookex.dto.exceptions.ApiError;
import com.example.bookex.dto.exceptions.FieldErrorEntry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 400 BAD REQUEST ---
    @ExceptionHandler({
            BadRequestException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ApiError handleBadRequest(Exception ex, HttpServletRequest req) {
        log.warn("400 Bad Request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, safeMessage(ex), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fieldErrors = new ArrayList<FieldErrorEntry>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.add(new FieldErrorEntry(fe.getField(), fe.getDefaultMessage()))
        );
        log.warn("400 Validation error on {} fields", fieldErrors.size());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, fieldErrors);
    }

    @ExceptionHandler(BindException.class)
    public ApiError handleBindException(BindException ex, HttpServletRequest req) {
        var fieldErrors = new ArrayList<FieldErrorEntry>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.add(new FieldErrorEntry(fe.getField(), fe.getDefaultMessage()))
        );
        log.warn("400 Bind validation error on {} fields", fieldErrors.size());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiError handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<FieldErrorEntry> list = ex.getConstraintViolations()
                .stream()
                .map(this::toFieldEntry)
                .toList();
        log.warn("400 Constraint violations: {}", list.size());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, list);
    }

    // --- 400 BAD REQUEST ---
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiError handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, safeMessage(ex), req, null);
    }

    // --- 401 UNAUTHORIZED ---
    @ExceptionHandler({ UnauthorizedException.class, AuthenticationException.class })
    public ApiError handleUnauthorized(Exception ex, HttpServletRequest req) {
        log.warn("401 Unauthorized: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, safeMessage(ex), req, null);
    }

    // --- 403 FORBIDDEN ---
    @ExceptionHandler({ ForbiddenException.class, AccessDeniedException.class, SecurityException.class })
    public ApiError handleForbidden(Exception ex, HttpServletRequest req) {
        log.warn("403 Forbidden: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, safeMessage(ex), req, null);
    }

    // --- 404 NOT FOUND ---
    @ExceptionHandler({ NotFoundException.class, java.util.NoSuchElementException.class })
    public ApiError handleNotFound(Exception ex, HttpServletRequest req) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, safeMessage(ex), req, null);
    }

    // --- 405 / 415 ---
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiError handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("405 Method Not Allowed: {}", ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiError handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        log.warn("415 Unsupported Media Type: {}", ex.getMessage());
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type", req, null);
    }

    // --- 409 CONFLICT ---
    @ExceptionHandler({ ConflictException.class, DataIntegrityViolationException.class })
    public ApiError handleConflict(Exception ex, HttpServletRequest req) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, safeMessage(ex), req, null);
    }

    // --- 413 PAYLOAD TOO LARGE ---
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiError handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        log.warn("413 Payload too large");
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "File too large", req, null);
    }

    // --- 500 INTERNAL SERVER ERROR ---
    @ExceptionHandler(Exception.class)
    public ApiError handleAll(Exception ex, HttpServletRequest req) {
        log.error("500 Internal Server Error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", req, null);
    }

    // helpers
    private ApiError build(HttpStatus status, String message, HttpServletRequest req, List<FieldErrorEntry> fields) {
        String path = req.getRequestURI();
        String requestId = getRequestId(req);
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .requestId(requestId)
                .fieldErrors(fields == null || fields.isEmpty() ? null : fields)
                .build();
    }

    private FieldErrorEntry toFieldEntry(ConstraintViolation<?> v) {
        String field = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
        return new FieldErrorEntry(field, v.getMessage());
    }

    private String getRequestId(HttpServletRequest req) {
        String rid = req.getHeader("X-Request-ID");
        return (rid == null || rid.isBlank()) ? UUID.randomUUID().toString() : rid;
    }

    private String safeMessage(Exception ex) {
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? ex.getClass().getSimpleName() : m;
    }
}
