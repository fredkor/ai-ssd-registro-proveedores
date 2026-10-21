package mx.com.fredkorlabs.registration.domain.model;

import java.time.Instant;

public record NoticeAcceptance(String version, Instant acceptedAt) {
    public NoticeAcceptance {
        if (version == null || version.isBlank()) throw new IllegalArgumentException("Notice version cannot be blank");
        if (acceptedAt == null) throw new IllegalArgumentException("Acceptance instant cannot be null");
    }
}
