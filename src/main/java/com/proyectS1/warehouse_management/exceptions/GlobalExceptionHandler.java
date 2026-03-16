package com.proyectS1.warehouse_management.exceptions;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
        ResponseStatusException exception,
        HttpServletRequest request
    ) {
        HttpStatusCode statusCode = exception.getStatusCode();
        log.warn(
            "Request failed with status {} on {} {}. Reason: {}",
            statusCode.value(),
            request.getMethod(),
            request.getRequestURI(),
            exception.getReason(),
            exception
        );
        return ResponseEntity.status(statusCode).body(buildErrorResponse(
            statusCode.value(),
            HttpStatus.valueOf(statusCode.value()).getReasonPhrase(),
            exception.getReason() != null ? exception.getReason() : "Request failed",
            request.getRequestURI(),
            List.of()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        List<String> details = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .toList();

        log.warn(
            "Validation failed on {} {}. Details: {}",
            request.getMethod(),
            request.getRequestURI(),
            details,
            exception
        );

        return ResponseEntity.badRequest().body(buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed",
            request.getRequestURI(),
            details
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
        ConstraintViolationException exception,
        HttpServletRequest request
    ) {
        List<String> details = exception.getConstraintViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .toList();

        log.warn(
            "Constraint validation failed on {} {}. Details: {}",
            request.getMethod(),
            request.getRequestURI(),
            details,
            exception
        );

        return ResponseEntity.badRequest().body(buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Constraint validation failed",
            request.getRequestURI(),
            details
        ));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequestExceptions(
        Exception exception,
        HttpServletRequest request
    ) {
        log.warn(
            "Malformed request on {} {}. Message: {}",
            request.getMethod(),
            request.getRequestURI(),
            exception.getMessage(),
            exception
        );
        return ResponseEntity.badRequest().body(buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Malformed request",
            request.getRequestURI(),
            List.of(exception.getMessage())
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
        DataIntegrityViolationException exception,
        HttpServletRequest request
    ) {
        log.error(
            "Database constraint validation failed on {} {}. Root cause: {}",
            request.getMethod(),
            request.getRequestURI(),
            extractDeepestMessage(exception),
            exception
        );
        return ResponseEntity.badRequest().body(buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Database constraint validation failed",
            request.getRequestURI(),
            List.of(extractDeepestMessage(exception))
        ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceededException(
        MaxUploadSizeExceededException exception,
        HttpServletRequest request
    ) {
        log.warn(
            "Uploaded file exceeded size limit on {} {}. Message: {}",
            request.getMethod(),
            request.getRequestURI(),
            exception.getMessage(),
            exception
        );
        return ResponseEntity.badRequest().body(buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Uploaded file exceeds the maximum allowed size",
            request.getRequestURI(),
            List.of("Maximum allowed size is 5 MB")
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
        Exception exception,
        HttpServletRequest request
    ) {
        String rootCauseMessage = extractDeepestMessage(exception);
        log.error(
            "Unexpected internal error on {} {}. Root cause: {}",
            request.getMethod(),
            request.getRequestURI(),
            rootCauseMessage,
            exception
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "Unexpected internal error",
            request.getRequestURI(),
            List.of(rootCauseMessage)
        ));
    }

    private ApiErrorResponse buildErrorResponse(
        int status,
        String error,
        String message,
        String path,
        List<String> details
    ) {
        return new ApiErrorResponse(
            Instant.now(),
            status,
            error,
            message,
            path,
            details
        );
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String extractDeepestMessage(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }

        return current.getMessage() != null ? current.getMessage() : "Unknown database error";
    }
}
