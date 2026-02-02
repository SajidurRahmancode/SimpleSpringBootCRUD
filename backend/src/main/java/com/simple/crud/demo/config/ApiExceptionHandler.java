package com.simple.crud.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice makes this class a global exception handler
// It intercepts exceptions thrown by any @RestController in the application
// Combines @ControllerAdvice and @ResponseBody (returns JSON automatically)
@RestControllerAdvice
public class ApiExceptionHandler {

    // Create a static final logger instance for this class
    // Static = shared across all instances, final = can't be changed
    // Used to log exception details for debugging and monitoring
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // @ExceptionHandler marks this method to handle MethodArgumentNotValidException
    // This exception is thrown when @Valid validation fails on request body objects
    // Example: when a user submits a form with invalid email format
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        // Create a map to store field-specific error messages
        Map<String, String> fieldErrors = new HashMap<>();
        // Loop through all field errors from the validation exception
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            // Add each field name as key and its error message as value
            // Example: {"email": "must be a valid email", "name": "must not be blank"}
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        // Log the validation failure at WARN level with endpoint and field errors
        log.warn("Validation failed - endpoint: {}, fieldErrors: {}", 
                request.getDescription(false), fieldErrors);
        
        // Create a response body map with error details
        Map<String, Object> body = new HashMap<>();
        // Add current timestamp in ISO-8601 format (e.g., "2026-02-01T10:15:30Z")
        body.put("timestamp", Instant.now().toString());
        // Add HTTP status code (400 = Bad Request)
        body.put("status", HttpStatus.BAD_REQUEST.value());
        // Add general error message
        body.put("error", "Validation failed");
        // Add the map of field-specific errors
        body.put("fieldErrors", fieldErrors);
        // Return HTTP 400 response with the error body as JSON
        return ResponseEntity.badRequest().body(body);
    }

    // @ExceptionHandler marks this method to handle MaxUploadSizeExceededException
    // This exception is thrown when uploaded file exceeds the configured size limit
    // Example: user tries to upload a 10MB file when limit is 5MB
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUpload(MaxUploadSizeExceededException ex) {
        // Log the rejection at WARN level with the exception message
        log.warn("File upload rejected - size exceeds maximum allowed: {}", ex.getMessage());
        // Return HTTP 400 response with error details
        // Using Map.of() for concise map creation (immutable map)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        // Add current timestamp
                        "timestamp", Instant.now().toString(),
                        // Add HTTP status code (400)
                        "status", HttpStatus.BAD_REQUEST.value(),
                        // Add user-friendly error message
                        "error", "File too large. Max 5MB"
                ));
    }

    // @ExceptionHandler marks this method to handle multiple exception types
    // MissingServletRequestParameterException: required query parameter is missing
    // MethodArgumentTypeMismatchException: parameter has wrong type (e.g., "abc" for an integer)
    @ExceptionHandler({ MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<?> handleBadRequest(Exception ex) {
        // Log the bad request at WARN level with exception type and message
        log.warn("Bad request - exceptionType: {}, message: {}", ex.getClass().getSimpleName(), ex.getMessage());
        // Return HTTP 400 response with error details
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        // Add current timestamp
                        "timestamp", Instant.now().toString(),
                        // Add HTTP status code (400 = Bad Request)
                        "status", HttpStatus.BAD_REQUEST.value(),
                        // Add the exception's error message
                        "error", ex.getMessage()
                ));
    }

    // @ExceptionHandler marks this method to handle AccessDeniedException
    // This exception is thrown when authenticated user lacks required role/permission
    // Example: regular USER tries to access an admin-only endpoint
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        // Log the security violation at WARN level (prefixed with "SECURITY:" for filtering)
        log.warn("SECURITY: Access denied - message: {}", ex.getMessage());
        // Return HTTP 403 response (Forbidden = authenticated but not authorized)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        // Add current timestamp
                        "timestamp", Instant.now().toString(),
                        // Add HTTP status code (403 = Forbidden)
                        "status", HttpStatus.FORBIDDEN.value(),
                        // Add generic error message (don't reveal specific permissions)
                        "error", "Access denied"
                ));
    }

    // @ExceptionHandler marks this method to handle AuthenticationException
    // This exception is thrown when authentication fails (invalid credentials, expired token, etc.)
    // Example: user provides wrong password or invalid JWT token
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuth(AuthenticationException ex) {
        // Log the authentication failure at WARN level with exception details
        log.warn("SECURITY: Authentication failed - exceptionType: {}, message: {}", 
                ex.getClass().getSimpleName(), ex.getMessage());
        // Return HTTP 401 response (Unauthorized = authentication required or failed)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        // Add current timestamp
                        "timestamp", Instant.now().toString(),
                        // Add HTTP status code (401 = Unauthorized)
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        // Add generic error message (don't reveal specific auth details for security)
                        "error", "Authentication required"
                ));
    }

    // @ExceptionHandler marks this method to handle RuntimeException (catch-all handler)
    // This catches any RuntimeException not handled by more specific handlers above
    // Example: NullPointerException, IllegalArgumentException, custom business exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex, WebRequest request) {
        // Log at ERROR level (more severe than WARN) with full stack trace
        // The last parameter 'ex' tells logger to include the full stack trace
        log.error("Runtime exception - endpoint: {}, exceptionType: {}, message: {}", 
                request.getDescription(false), ex.getClass().getSimpleName(), ex.getMessage(), ex);
        
        // Create a response body map with error details
        Map<String, Object> body = new HashMap<>();
        // Add current timestamp
        body.put("timestamp", Instant.now().toString());
        // Add HTTP status code (400 = Bad Request)
        body.put("status", HttpStatus.BAD_REQUEST.value());
        // Add the exception's error message (useful for debugging)
        body.put("error", ex.getMessage());
        // Return HTTP 400 response with the error body as JSON
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
