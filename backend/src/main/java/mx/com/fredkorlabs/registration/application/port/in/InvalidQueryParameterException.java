package mx.com.fredkorlabs.registration.application.port.in;

/** A query parameter that the use case cannot accept: VALIDATION_ERROR with {@code parameter} (ADR-0019). */
public class InvalidQueryParameterException extends RuntimeException {

    private final String parameter;
    private final String reason;

    public InvalidQueryParameterException(String parameter, String reason) {
        super("Request parameter " + parameter + " " + reason);
        this.parameter = parameter;
        this.reason = reason;
    }

    public String getParameter() {
        return parameter;
    }

    public String getReason() {
        return reason;
    }
}
