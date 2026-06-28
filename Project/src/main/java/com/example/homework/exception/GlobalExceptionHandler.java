package com.example.homework.exception;

import com.example.homework.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        String message = localize(ex.getMessageKey(), ex.getArgs());
        log.warn("Resource not found: {}", message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        String message = localize(ex.getMessageKey(), ex.getArgs());
        log.warn("Duplicate resource rejected: {}", message);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Field messages are already localized by the MessageSource-backed validator
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        log.debug("Request body validation failed with {} error(s): {}", errors.size(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(localize("error.validation.failed"))
                        .errors(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        log.debug("Malformed request body rejected: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(localize("error.request.malformed"))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        // Triggered by method-level @PreAuthorize denials that reach the dispatcher
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .message(localize("error.access.denied"))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Conflicting request rejected: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception while processing request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(localize("error.unexpected"))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /** Resolves a bundle key against the locale of the current request. */
    private String localize(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, locale);
    }
}
