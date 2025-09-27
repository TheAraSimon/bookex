package com.example.bookex.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.NoSuchElementException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    // --- 400 BAD REQUEST ---
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ModelAndView handleBadRequest(Exception ex, HttpServletRequest req) {
        log.warn("400 Bad Request: {}", ex.getMessage());
        return errorView(HttpStatus.BAD_REQUEST, safe(ex.getMessage()), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = "Validation failed";
        if (ex.getBindingResult() != null && !ex.getBindingResult().getFieldErrors().isEmpty()) {
            msg += ": " + ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        log.warn("400 Validation error: {}", msg);
        return errorView(HttpStatus.BAD_REQUEST, msg, req);
    }

    @ExceptionHandler(BindException.class)
    public ModelAndView handleBindException(BindException ex, HttpServletRequest req) {
        String msg = "Validation failed";
        if (!ex.getFieldErrors().isEmpty()) {
            msg += ": " + ex.getFieldErrors().get(0).getDefaultMessage();
        }
        log.warn("400 Bind validation error: {}", msg);
        return errorView(HttpStatus.BAD_REQUEST, msg, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream().findFirst()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .orElse("Validation failed");
        log.warn("400 Constraint violation: {}", msg);
        return errorView(HttpStatus.BAD_REQUEST, msg, req);
    }

    // --- 401 UNAUTHORIZED ---
    @ExceptionHandler(AuthenticationException.class)
    public String handleUnauthorized(AuthenticationException ex) {
        log.warn("401 Unauthorized: {}", ex.getMessage());
        return "redirect:/login?error";
    }

    // --- 403 FORBIDDEN ---
    @ExceptionHandler({AccessDeniedException.class, SecurityException.class })
    public ModelAndView handleForbidden(Exception ex, HttpServletRequest req) {
        log.warn("403 Forbidden: {}", ex.getMessage());
        return errorView(HttpStatus.FORBIDDEN, "Forbidden", req);
    }

    // --- 404 NOT FOUND ---
    @ExceptionHandler({ NotFoundException.class, NoSuchElementException.class })
    public ModelAndView handleNotFound(Exception ex, HttpServletRequest req) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return errorView(HttpStatus.NOT_FOUND, safe(ex.getMessage()), req);
    }

    // --- 405 / 415 ---
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("405 Method Not Allowed: {}", ex.getMessage());
        return errorView(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ModelAndView handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        log.warn("415 Unsupported Media Type: {}", ex.getMessage());
        return errorView(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type", req);
    }

    // --- 413 PAYLOAD TOO LARGE ---
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        log.warn("413 Payload too large");
        return errorView(HttpStatus.PAYLOAD_TOO_LARGE, "File too large", req);
    }

    // --- 500 INTERNAL SERVER ERROR ---
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAll(Exception ex, HttpServletRequest req) {
        log.error("500 Internal Server Error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return errorView(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", req);
    }

    // helpers
    private ModelAndView errorView(HttpStatus status, String message, HttpServletRequest req) {
        ModelAndView mv = new ModelAndView("error"); // templates/error.html
        mv.setStatus(status);
        mv.addObject("status", status.value());
        mv.addObject("error", status.getReasonPhrase());
        mv.addObject("message", message);
        mv.addObject("path", req.getRequestURI());
        mv.addObject("timestamp", Instant.now());
        return mv;
    }

    private String safe(String m) {
        return (m == null || m.isBlank()) ? "Error" : m;
    }
}
