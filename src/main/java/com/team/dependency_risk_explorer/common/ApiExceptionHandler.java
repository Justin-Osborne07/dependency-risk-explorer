package com.team.dependency_risk_explorer.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.ArrayList;

import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(
            int status,
            String message,
            List<String> details
    ) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException e) {

        ErrorResponse error = new ErrorResponse(
                400,
                e.getMessage(),
                List.of()
        );

        return ResponseEntity.status(400).body(error);
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoSuchElementException e) {

        ErrorResponse error = new ErrorResponse(
                404,
                e.getMessage(),
                List.of()
        );

        return ResponseEntity.status(404).body(error);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(
            MaxUploadSizeExceededException e) {

        ErrorResponse error = new ErrorResponse(
                413,
                "Import file exceeds the allowed size",
                List.of()
        );

        return ResponseEntity.status(413).body(error);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalError(
            Exception e) {

        ErrorResponse error = new ErrorResponse(
                500,
                "Internal server error",
                List.of()
        );

        return ResponseEntity.status(500).body(error);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e) {

        List<String> details = new ArrayList<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                400,
                "Validation failed",
                details
        );

        return ResponseEntity.status(400).body(response);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException e) {

        ErrorResponse error = new ErrorResponse(
                400,
                "Invalid JSON request",
                List.of()
        );

        return ResponseEntity.status(400).body(error);
    }
}