package mx.com.fredkorlabs.registration.application.port.out;

public record SignatureEvidenceReference(String value) {
    public SignatureEvidenceReference {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Evidence reference cannot be blank");
    }
}
