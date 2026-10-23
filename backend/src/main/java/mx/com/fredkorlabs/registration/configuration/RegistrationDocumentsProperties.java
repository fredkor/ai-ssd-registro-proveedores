package mx.com.fredkorlabs.registration.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** ADR-0010: root of the documents volume ({@code REGISTRATION_DOCUMENTS_ROOT}, {@code /repository} by default). */
@ConfigurationProperties(prefix = "registration.documents")
public record RegistrationDocumentsProperties(String rootLocation) {
}
