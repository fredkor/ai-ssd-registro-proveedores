package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidNoticeVersionException;

import java.time.Instant;

public record NoticeAcceptance(String version, Instant acceptedAt) {

    public static final String CURRENT_VERSION = "1.0";

    public NoticeAcceptance {
        if (!CURRENT_VERSION.equals(version)) throw new InvalidNoticeVersionException("Notice version must be " + CURRENT_VERSION);
        if (acceptedAt == null) throw new IllegalArgumentException("Acceptance instant cannot be null");
    }
}
