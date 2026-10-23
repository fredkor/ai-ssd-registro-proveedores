package mx.com.fredkorlabs.registration.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import mx.com.fredkorlabs.registration.adapter.in.web.ProblemResponses.FieldProblem;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ErrorCode;
import mx.com.fredkorlabs.registration.application.port.in.FiscalDocumentNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.InvalidIdentificationDataException;
import mx.com.fredkorlabs.registration.application.port.in.InvalidQueryParameterException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationAccessDeniedException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationAlreadyExistsException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationNotFoundException;
import mx.com.fredkorlabs.registration.application.port.in.RegistrationSessionUnavailableException;
import mx.com.fredkorlabs.registration.application.port.in.SignatureVerificationFailedException;
import mx.com.fredkorlabs.registration.domain.exception.ApplicantRfcMismatchException;
import mx.com.fredkorlabs.registration.domain.exception.DocumentFormatNotSupportedException;
import mx.com.fredkorlabs.registration.domain.exception.DocumentTooLargeException;
import mx.com.fredkorlabs.registration.domain.exception.FiscalDocumentAlreadyExistsException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidCertificateIdentityException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidNoticeVersionException;
import mx.com.fredkorlabs.registration.domain.exception.InvalidRegistrationStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
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
            .map(error -> FieldProblem.atPointer(JsonPointers.fromPropertyPath(error.getField()),
                Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid")))
            .toList();
        return validationError("Request body does not match the contract", errors, request);
    }

    @ExceptionHandler(MissingRequestPropertyException.class)
    public ResponseEntity<ProblemDetail> handleMissingProperty(MissingRequestPropertyException ex,
                                                               HttpServletRequest request) {
        return validationError("Request body does not match the contract",
            List.of(FieldProblem.atPointer(ex.getPointer(), "is required")), request);
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
            List.of(FieldProblem.atPointer(pointer, reasonOf(cause))), request);
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

    @ExceptionHandler(RegistrationAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateRegistration(RegistrationAlreadyExistsException ex,
                                                                     HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.DUPLICATE_RFC, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(RegistrationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleRegistrationNotFound(RegistrationNotFoundException ex,
                                                                    HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.REGISTRATION_NOT_FOUND, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(FiscalDocumentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleFiscalDocumentNotFound(FiscalDocumentNotFoundException ex,
                                                                      HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.FISCAL_DOCUMENT_NOT_FOUND, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(RegistrationAccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(RegistrationAccessDeniedException ex,
                                                            HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.REGISTRATION_ACCESS_DENIED, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(InvalidRegistrationStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidState(InvalidRegistrationStateException ex,
                                                            HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.INVALID_STATE_TRANSITION, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(FiscalDocumentAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleFiscalDocumentExists(FiscalDocumentAlreadyExistsException ex,
                                                                    HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.FISCAL_DOCUMENT_ALREADY_EXISTS, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(DocumentTooLargeException.class)
    public ResponseEntity<ProblemDetail> handleDocumentTooLarge(DocumentTooLargeException ex,
                                                                HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.DOCUMENT_TOO_LARGE, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(DocumentFormatNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleDocumentFormat(DocumentFormatNotSupportedException ex,
                                                              HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.DOCUMENT_FORMAT_NOT_SUPPORTED, ex.getMessage(), List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(RegistrationSessionUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleSessionUnavailable(RegistrationSessionUnavailableException ex,
                                                                  HttpServletRequest request) {
        ResponseEntity<ProblemDetail> response = ProblemResponses.response(ErrorCode.REGISTRATION_TOKEN_INVALID,
            "Registration token is missing or invalid", List.of(), request.getRequestURI());
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders())
            .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer").body(response.getBody());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ProblemDetail> handleUnknownRoute(Exception ex, HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.RESOURCE_NOT_FOUND, "No operation exists at this path",
            List.of(), request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        ResponseEntity<ProblemDetail> response = ProblemResponses.response(ErrorCode.METHOD_NOT_ALLOWED,
            "HTTP method is not supported for this path", List.of(), request.getRequestURI());
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(response.getHeaders());
        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(ex.getSupportedHttpMethods());
        }
        return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                             HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.NOT_ACCEPTABLE,
            "Accept header does not allow any representation produced by this operation", List.of(),
            request.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
            "Content-Type is not supported by this operation", List.of(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleParameterType(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        return validationError("Request parameter is invalid",
            List.of(FieldProblem.atParameter(ex.getName(), "has an invalid format")), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                HttpServletRequest request) {
        return validationError("Request parameter is required",
            List.of(FieldProblem.atParameter(ex.getParameterName(), "is required")), request);
    }

    @ExceptionHandler(InvalidRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRequestParameter(InvalidRequestParameterException ex,
                                                                       HttpServletRequest request) {
        return validationError("Request parameter is invalid",
            List.of(FieldProblem.atParameter(ex.getParameter(), ex.getReason())), request);
    }

    @ExceptionHandler(InvalidQueryParameterException.class)
    public ResponseEntity<ProblemDetail> handleInvalidQueryParameter(InvalidQueryParameterException ex,
                                                                     HttpServletRequest request) {
        return validationError("Request parameter is invalid",
            List.of(FieldProblem.atParameter(ex.getParameter(), ex.getReason())), request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ProblemDetail> handleMissingPart(MissingServletRequestPartException ex,
                                                           HttpServletRequest request) {
        return validationError("Request part is required",
            List.of(FieldProblem.atParameter(ex.getRequestPartName(), "is required")), request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleParameterValidation(HandlerMethodValidationException ex,
                                                                   HttpServletRequest request) {
        List<FieldProblem> errors = ex.getParameterValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream().map(error -> FieldProblem.atParameter(
                Objects.requireNonNullElse(result.getMethodParameter().getParameterName(), "parameter"),
                Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid"))))
            .toList();
        return validationError("Request parameter is invalid", errors, request);
    }

    // The generated interfaces are @Validated: constrained parameters are checked by a method-validation proxy.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleParameterConstraint(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        List<FieldProblem> errors = ex.getConstraintViolations().stream()
            .map(violation -> FieldProblem.atParameter(lastNodeName(violation.getPropertyPath()),
                Objects.requireNonNullElse(violation.getMessage(), "is invalid")))
            .toList();
        return validationError("Request parameter is invalid", errors, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleUploadTooLarge(MaxUploadSizeExceededException ex,
                                                              HttpServletRequest request) {
        return ProblemResponses.response(ErrorCode.DOCUMENT_TOO_LARGE, "Document exceeds the maximum size",
            List.of(), request.getRequestURI());
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

    private static String lastNodeName(Path path) {
        String name = "parameter";
        for (Path.Node node : path) {
            if (node.getName() != null) name = node.getName();
        }
        return name;
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
