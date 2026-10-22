package mx.com.fredkorlabs.registration.application.port.in;

/** RFC of the accepted identification, taken from the validated registration token (ADR-0015). */
public record CheckRegistrationExistenceQuery(String rfc) {}
