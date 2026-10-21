package mx.com.fredkorlabs.registration.adapter.in.web;

/** A property defined by the request schema was omitted (ADR-0009). */
public class MissingRequestPropertyException extends RuntimeException {

    private final String propertyPath;

    public MissingRequestPropertyException(String propertyPath) {
        super("Missing request property: " + propertyPath);
        this.propertyPath = propertyPath;
    }

    public String getPropertyPath() {
        return propertyPath;
    }
}
