package mx.com.fredkorlabs.registration.domain.model;

public record SectionProgress(RegistrationSection section, SectionStatus status) {
    public SectionProgress {
        if (section == null) throw new IllegalArgumentException("Section cannot be null");
        if (status == null) throw new IllegalArgumentException("Status cannot be null");
    }
}
