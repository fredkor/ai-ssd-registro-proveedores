package mx.com.fredkorlabs.registration.domain.model;

/** A CUCoP rule broken by one field; {@code field} uses the submission names, e.g. {@code specificItemIds/2}. */
public record CucopParticipationViolation(String field, String reason) {}
