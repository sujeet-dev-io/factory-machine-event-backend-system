package com.file.intern.assignment.exception;

import com.file.intern.assignment.service.EventIngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Validation Exceptions (Ingestion)
    @ExceptionHandler(EventIngestionService.ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            EventIngestionService.ValidationException ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("error", "VALIDATION_ERROR");
        body.put("reason", ex.getReason());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

   // Illegal Arguments (Stats Time Window etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("error", "INVALID_REQUEST");
        body.put("message", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

  //  Bean Validation / JSON Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("error", "INVALID_PAYLOAD");
        body.put("message", "Request payload validation failed");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

  //  Fallback (Unexpected Errors)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", "Something went wrong");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
