package com.chengukargbo.careeros.common.exception;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.chengukargbo.careeros.companies.CompanyNotFoundException;
import com.chengukargbo.careeros.jobs.JobOpportunityNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ApiError> handleCompanyNotFound(
        CompanyNotFoundException exception,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI()
        );
    }
    @ExceptionHandler(JobOpportunityNotFoundException.class)
    public ResponseEntity<ApiError> handleJobOpportunityNotFound(
        JobOpportunityNotFoundException exception,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("Request validation failed");

        return buildError(
            HttpStatus.BAD_REQUEST,
            message,
            request.getRequestURI()
        );
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiError> handleBusinessValidation(
        BusinessValidationException exception,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    private ResponseEntity<ApiError> buildError(
        HttpStatus status,
        String message,
        String path
    ) {
        ApiError error = new ApiError(
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            OffsetDateTime.now()
        );

        return ResponseEntity.status(status).body(error);
    }
}