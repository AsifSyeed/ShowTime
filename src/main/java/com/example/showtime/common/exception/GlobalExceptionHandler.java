package com.example.showtime.common.exception;
import com.example.showtime.common.model.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Create an ErrorResponse object with the error message
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

        // Create a ResponseEntity with the ErrorResponse and HttpStatus
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        // Create an ErrorResponse object with the "Unauthorized" message
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

        // Create a ResponseEntity with the ErrorResponse and HttpStatus
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(BaseException ex) {
        ApiResponse<Object> response = new ApiResponse<>(ex.getResponseCode(), ex.getMessage(), null);
        return ResponseEntity.status(ex.getResponseCode()).body(response);
    }
}
