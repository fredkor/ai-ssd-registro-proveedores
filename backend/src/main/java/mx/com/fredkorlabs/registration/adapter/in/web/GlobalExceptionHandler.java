package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ErrorCode;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ErrorResponse;
import mx.com.fredkorlabs.registration.domain.exception.InvalidNoticeVersionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();
        return validationError("Invalid request", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return validationError("Malformed request body", List.of());
    }

    @ExceptionHandler(InvalidNoticeVersionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNoticeVersion(InvalidNoticeVersionException ex) {
        return validationError(ex.getMessage(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError().body(
            new ErrorResponse(ErrorCode.INTERNAL_ERROR, "Unexpected error", List.of())
        );
    }

    private ResponseEntity<ErrorResponse> validationError(String message, List<String> details) {
        return ResponseEntity.badRequest().body(
            new ErrorResponse(ErrorCode.VALIDATION_ERROR, message, details)
        );
    }
}
