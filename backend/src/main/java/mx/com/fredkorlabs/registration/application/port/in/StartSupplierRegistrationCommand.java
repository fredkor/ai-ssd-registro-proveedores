package mx.com.fredkorlabs.registration.application.port.in;

/** {@code registrationSessionId} is the jti of the validated registration token (ADR-0015). */
public record StartSupplierRegistrationCommand(String registrationSessionId) {}
