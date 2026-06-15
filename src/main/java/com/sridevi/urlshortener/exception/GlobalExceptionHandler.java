package com.sridevi.urlshortener.exception;

import com.sridevi.urlshortener.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BadRequestException.class) ResponseEntity<ErrorResponse> badRequest(Exception e, HttpServletRequest r) { return response(HttpStatus.BAD_REQUEST, e, r, Map.of()); }
    @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ErrorResponse> notFound(Exception e, HttpServletRequest r) { return response(HttpStatus.NOT_FOUND, e, r, Map.of()); }
    @ExceptionHandler(ExpiredUrlException.class) ResponseEntity<ErrorResponse> gone(Exception e, HttpServletRequest r) { return response(HttpStatus.GONE, e, r, Map.of()); }
    @ExceptionHandler(ConflictException.class) ResponseEntity<ErrorResponse> conflict(Exception e, HttpServletRequest r) { return response(HttpStatus.CONFLICT, e, r, Map.of()); }
    @ExceptionHandler(ForbiddenException.class) ResponseEntity<ErrorResponse> forbidden(Exception e, HttpServletRequest r) { return response(HttpStatus.FORBIDDEN, e, r, Map.of()); }
    @ExceptionHandler(BadCredentialsException.class) ResponseEntity<ErrorResponse> unauthorized(Exception e, HttpServletRequest r) { return response(HttpStatus.UNAUTHORIZED, new RuntimeException("Invalid username or password"), r, Map.of()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(
                f -> f.getField(), f -> Optional.ofNullable(f.getDefaultMessage()).orElse("invalid"), (a, b) -> a, LinkedHashMap::new));
        return response(HttpStatus.BAD_REQUEST, new RuntimeException("Validation failed"), r, errors);
    }
    @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> unexpected(Exception e, HttpServletRequest r) {
        log.error("Unhandled error for {} {}", r.getMethod(), r.getRequestURI(), e);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException("An unexpected error occurred"), r, Map.of());
    }
    private ResponseEntity<ErrorResponse> response(HttpStatus status, Exception e, HttpServletRequest r, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), e.getMessage(), r.getRequestURI(), errors));
    }
    
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(
            TooManyRequestsException ex,
            HttpServletRequest request
    ) {

        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;

        return ResponseEntity.status(status)
                .body(
                        new ErrorResponse(
                                Instant.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                ex.getMessage(),
                                request.getRequestURI(),
                                null
                        )
                );
    }
    
}
