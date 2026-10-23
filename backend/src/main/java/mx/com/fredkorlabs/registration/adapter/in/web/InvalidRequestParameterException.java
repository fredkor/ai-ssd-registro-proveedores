package mx.com.fredkorlabs.registration.adapter.in.web;

/** A query parameter that is not defined by the operation or is repeated (ADR-0020). */
public class InvalidRequestParameterException extends RuntimeException {

    private final String parameter;
    private final String reason;

    public InvalidRequestParameterException(String parameter, String reason) {
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
