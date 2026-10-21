package mx.com.fredkorlabs.registration.domain.model;

public class FiscalIdentificationDocument {

    private final SupplierRegistrationId registrationId;
    private final String originalName;
    private final StoredDocumentReference storedFile;

    public FiscalIdentificationDocument(SupplierRegistrationId registrationId,
                                        String originalName,
                                        StoredDocumentReference storedFile) {
        if (registrationId == null) throw new IllegalArgumentException("Registration ID cannot be null");
        if (originalName == null || originalName.isBlank()) throw new IllegalArgumentException("Original name cannot be blank");
        if (storedFile == null) throw new IllegalArgumentException("Stored file reference cannot be null");
        this.registrationId = registrationId;
        this.originalName = originalName;
        this.storedFile = storedFile;
    }

    public SupplierRegistrationId registrationId() { return registrationId; }
    public String originalName() { return originalName; }
    public StoredDocumentReference storedFile() { return storedFile; }
}
