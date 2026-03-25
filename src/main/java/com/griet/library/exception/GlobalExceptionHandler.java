package com.griet.library.exception;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Duplicate accession number / ISBN (unique constraint)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMessage() != null && ex.getMessage().contains("accession")
                ? "Accession number already exists"
                : ex.getMessage() != null && ex.getMessage().contains("isbn")
                ? "ISBN already exists"
                : "Duplicate entry — check accession number or ISBN";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", msg));
    }

    // Malformed JSON / wrong date format
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "Invalid request format — check date fields (use YYYY-MM-DD)"));
    }

    // General runtime errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", ex.getMessage() != null
                        ? ex.getMessage()
                        : "An unexpected error occurred"));
    }
}