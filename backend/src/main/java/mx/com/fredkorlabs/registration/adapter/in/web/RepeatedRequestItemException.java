package mx.com.fredkorlabs.registration.adapter.in.web;

import java.util.List;

/** An item of a uniqueItems array repeats an earlier one (INV-31); the pointer names the repetition. */
public class RepeatedRequestItemException extends RuntimeException {

    private final String pointer;

    public RepeatedRequestItemException(List<String> path) {
        super("Repeated request item: " + JsonPointers.of(path));
        this.pointer = JsonPointers.of(path);
    }

    public String getPointer() {
        return pointer;
    }
}
