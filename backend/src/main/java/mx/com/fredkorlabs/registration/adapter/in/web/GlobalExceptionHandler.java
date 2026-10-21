package mx.com.fredkorlabs.registration.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.ProblemResponses.FieldProblem;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ErrorCode;
import mx.com.fredkorlabs.registration.application.port.in.InvalidIdentificationDataException;
import mx.com.fredkorlabs.registration.application.port.in.SignatureVerificationFailedException;
import mx.com.fredkorlabs.registration.domain.exception.ApplicantRfcMismatchException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidCertificateIdentityException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidNoticeVersionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.exc.ValueInstantiationException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Translates every error into Problem Details (ADR-0019) with the ADR-0004 codes. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleInvalidArgument(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {
        List<FieldProblem> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldProblem(JsonPointers.fromPropertyPath(error.getField()),
                Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid")))
            .toList();
        return validationError("Request body does not match the contract", errors, request);
    }

    @ExceptionHandler(MissingRequestPropertyException.class)
    public ResponseEntity<ProblemDetail> handleMissingProperty(MissingRequestPropertyException ex,
                                                               HttpServletRequest request) {
        return validationError("Request body does not match the contract",
            List.of(new FieldProblem(ex.getPointer(), "is required")), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        JacksonException cause = jacksonCause(ex);
        if (cause == null) {
            return validationError("Request body is required", List.of(), request);
        }
        if (cause instanceof StreamReadException) {
            return validationError("Request body is not valid JSON", List.of(), request);
        }
        String pointer = JsonPointers.of(cause.getPath().stream()
            .map(reference -> reference.getPropertyName() != null
                ? reference.getPropertyName() : String.valueOf(reference.getIndex()))
            .toList());
        return validationError("Request body does not match the contract",
            List.of(new FieldProblem(pointer, reasonOf(cause))), request);
    }

    @ExceptionHandler({InvalidNoticeVersionException.class, InvalidIdentificationDataException.class,
        InvalidCertificateIdentityException.class})
    public ResponseEntity<ProblemDetail> handleInvalidData(RuntimeException ex, HttpServletRequest request) {
        return validationError(ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler({SignatureVerificationFailedException.class, ApplicantRfcMismatchException.class})
    public ResponseEntity<ProblemDetail> handleSignatureVerificationFailed(RuntimeException ex,
                                                                           HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.SIGNATURE_VERIFICATION_FAILED, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return ProblemResponses.response(ErrorCode.INTERNAL_ERROR, "Unexpected error", List.of(),
            request.getRequestURI());
    }

    private static ResponseEntity<ProblemDetail> validationError(String detail, List<FieldProblem> errors,
                                                                 HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.VALIDATION_ERROR, detail, errors, request.getRequestURI());
    }

    private static JacksonException jacksonCause(Throwable error) {
        for (Throwable cause = error.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof JacksonException jackson) return jackson;
        }
        return null;
    }

    // Short reasons that never echo the received value.
    private static String reasonOf(JacksonException cause) {
        if (cause instanceof UnrecognizedPropertyException) {
            return "is not a defined property";
        }
        Class<?> target = switch (cause) {
            case MismatchedInputException mismatch -> mismatch.getTargetType();
            case ValueInstantiationException instantiation -> instantiation.getType().getRawClass();
            default -> null;
        };
        if (target != null && target.isEnum()) {
            return "must be one of " + Arrays.toString(target.getEnumConstants());
        }
        return cause instanceof InvalidFormatException ? "has an invalid format" : "has an invalid type";
    }
}
