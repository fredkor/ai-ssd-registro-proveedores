package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ErrorCode;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError().body(
            new ErrorResponse(ErrorCode.INTERNAL_ERROR, "Unexpected error", List.of())
        );
    }
}
