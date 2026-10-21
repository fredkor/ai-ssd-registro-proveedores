package mx.com.fredkorlabs.registration.adapter.in.web;

import java.util.List;

/** JSON Pointer (RFC 6901) for the {@code errors} extension of ADR-0019. */
final class JsonPointers {

    private JsonPointers() {
    }

    /** Segments are property names or array indexes, from the document root. */
    static String of(List<String> segments) {
        StringBuilder pointer = new StringBuilder();
        for (String segment : segments) {
            pointer.append('/').append(segment.replace("~", "~0").replace("/", "~1"));
        }
        return pointer.toString();
    }

    /** Converts a Bean Validation property path such as {@code signatureResult.items[0].rfc}. */
    static String fromPropertyPath(String propertyPath) {
        return of(List.of(propertyPath.replace("[", ".").replace("]", "").split("\\.")));
    }
}
