package com.springbootacademy.api.exception;

import com.springbootacademy.api.dto.StandardResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler. Every error is returned using the StandardResponse
 * envelope with status = "FAIL".
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<StandardResponse<?>> build(HttpStatus httpStatus, String message, Object data) {
        return ResponseEntity.status(httpStatus).body(StandardResponse.fail(message, data));
    }

    // ---------- 404 ----------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardResponse<?>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<StandardResponse<?>> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Requested resource was not found: /" + ex.getResourcePath(), null);
    }

    // ---------- 400 : business rules ----------
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardResponse<?>> handleBusiness(BusinessException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // ---------- 409 : duplicates ----------
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<StandardResponse<?>> handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    /** Safety net for races that slip past the explicit duplicate checks (unique constraints). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardResponse<?>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT,
                "Operation violates a data constraint (for example a duplicate email or course code)", null);
    }

    // ---------- 400 : validation ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String msg = fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage();
            errors.merge(fe.getField(), msg, (a, b) -> a + "; " + b);
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            errors.put(v.getPropertyPath().toString(), v.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardResponse<?>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Malformed JSON request or invalid field value (check enum values, numbers and dates)", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        return build(HttpStatus.BAD_REQUEST, message, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StandardResponse<?>> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "Required parameter '" + ex.getParameterName() + "' is missing", null);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<StandardResponse<?>> handleInvalidSort(PropertyReferenceException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid sort property: '" + ex.getPropertyName() + "'", null);
    }

    /** Some Spring Data versions wrap an invalid sort property in InvalidDataAccessApiUsageException. */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<StandardResponse<?>> handleInvalidDataAccessUsage(InvalidDataAccessApiUsageException ex) {
        if (ex.getCause() instanceof PropertyReferenceException pre) {
            return handleInvalidSort(pre);
        }
        return handleUnexpected(ex);
    }

    // ---------- 405 ----------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<StandardResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), null);
    }

    // ---------- 500 ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<?>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", null);
    }
}
