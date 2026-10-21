package mx.com.fredkorlabs.registration.application.port.out;

public record NoticeAcceptanceReference(String value) {
    public NoticeAcceptanceReference {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Acceptance reference cannot be blank");
    }
}
