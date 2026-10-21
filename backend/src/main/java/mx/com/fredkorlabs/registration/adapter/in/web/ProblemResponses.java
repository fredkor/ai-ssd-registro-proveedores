package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Locale;

/** ADR-0019: Problem Details (RFC 9457) with the ADR-0004 code table. */
final class ProblemResponses {

    static final String TYPE_PREFIX = "urn:fredkorlabs:registration:problem:";

    /** One entry of the {@code errors} extension: JSON Pointer into the request body plus a short reason. */
    record FieldProblem(String pointer, String detail) {}

    private ProblemResponses() {
    }

    static ResponseEntity<ProblemDetail> response(ErrorCode code, String detail, List<FieldProblem> errors,
                                                  String instance) {
        ProblemDetail problem = problem(code, detail, errors, instance);
        return ResponseEntity.status(problem.getStatus())
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    static ProblemDetail problem(ErrorCode code, String detail, List<FieldProblem> errors, String instance) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(statusOf(code), detail);
        problem.setType(URI.create(TYPE_PREFIX + code.getValue().toLowerCase(Locale.ROOT).replace('_', '-')));
        problem.setTitle(titleOf(code));
        problem.setInstance(URI.create(instance));
        problem.setProperty("code", code.getValue());
        problem.setProperty("errors", List.copyOf(errors));
        return problem;
    }

    static HttpStatus statusOf(ErrorCode code) {
        return switch (code) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case REGISTRATION_TOKEN_INVALID -> HttpStatus.UNAUTHORIZED;
            case REGISTRATION_ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case REGISTRATION_NOT_FOUND, FISCAL_DOCUMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_RFC, INVALID_STATE_TRANSITION, FISCAL_DOCUMENT_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case DOCUMENT_TOO_LARGE -> HttpStatus.CONTENT_TOO_LARGE;
            case DOCUMENT_FORMAT_NOT_SUPPORTED -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case SIGNATURE_VERIFICATION_FAILED, REGISTRATION_INCOMPLETE -> HttpStatus.UNPROCESSABLE_CONTENT;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    static String titleOf(ErrorCode code) {
        return switch (code) {
            case VALIDATION_ERROR -> "Invalid request";
            case REGISTRATION_TOKEN_INVALID -> "Invalid registration token";
            case REGISTRATION_ACCESS_DENIED -> "Registration access denied";
            case REGISTRATION_NOT_FOUND -> "Registration not found";
            case FISCAL_DOCUMENT_NOT_FOUND -> "Fiscal document not found";
            case DUPLICATE_RFC -> "Duplicate RFC";
            case INVALID_STATE_TRANSITION -> "Invalid state transition";
            case FISCAL_DOCUMENT_ALREADY_EXISTS -> "Fiscal document already exists";
            case DOCUMENT_TOO_LARGE -> "Document too large";
            case DOCUMENT_FORMAT_NOT_SUPPORTED -> "Document format not supported";
            case SIGNATURE_VERIFICATION_FAILED -> "Signature verification failed";
            case REGISTRATION_INCOMPLETE -> "Registration incomplete";
            case INTERNAL_ERROR -> "Internal error";
        };
    }
}
