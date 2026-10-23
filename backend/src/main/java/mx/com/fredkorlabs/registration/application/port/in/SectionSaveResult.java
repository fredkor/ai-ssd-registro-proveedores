package mx.com.fredkorlabs.registration.application.port.in;

import java.util.UUID;

/** A section saved by Next: its progress and the section that follows (RF-REG-05). */
public record SectionSaveResult(UUID registrationId, String section, String status, String nextSection) {}
