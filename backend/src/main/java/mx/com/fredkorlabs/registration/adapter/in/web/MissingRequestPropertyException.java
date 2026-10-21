package mx.com.fredkorlabs.registration.adapter.in.web;

import java.util.List;

/** A property defined by the request schema was omitted (ADR-0009). */
public class MissingRequestPropertyException extends RuntimeException {

    private final String pointer;

    public MissingRequestPropertyException(List<String> path) {
        super("Missing request property: " + JsonPointers.of(path));
        this.pointer = JsonPointers.of(path);
    }

    public String getPointer() {
        return pointer;
    }
}
