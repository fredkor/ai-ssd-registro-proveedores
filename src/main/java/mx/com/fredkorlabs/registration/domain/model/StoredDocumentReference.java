package mx.com.fredkorlabs.registration.domain.model;

import java.util.UUID;

public record StoredDocumentReference(UUID value) {
    public StoredDocumentReference {
        if (value == null) throw new IllegalArgumentException("Document reference cannot be null");
    }
}
