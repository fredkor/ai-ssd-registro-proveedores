package mx.com.fredkorlabs.registration.adapter.in.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * ADR-0019: errors raised outside controllers (for example inside the security filter chain) reach the
 * servlet error path; answer them with Problem Details instead of the framework's default body.
 */
@RestController
public class ProblemErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ProblemErrorController.class);

    @RequestMapping("${server.error.path:/error}")
    public ResponseEntity<ProblemDetail> error(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String instance = Objects.toString(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI),
            request.getRequestURI());
        ErrorCode code = switch (status instanceof Integer value ? value : 500) {
            case 404 -> ErrorCode.RESOURCE_NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 406 -> ErrorCode.NOT_ACCEPTABLE;
            case 413 -> ErrorCode.DOCUMENT_TOO_LARGE;
            case 415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
            default -> ErrorCode.INTERNAL_ERROR;
        };
        if (code == ErrorCode.INTERNAL_ERROR) {
            log.error("Unexpected error outside controllers", (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
            return ProblemResponses.response(code, "Unexpected error", List.of(), instance);
        }
        return ProblemResponses.response(code, ProblemResponses.titleOf(code), List.of(), instance);
    }
}
