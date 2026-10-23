package mx.com.fredkorlabs.registration.domain.model;

/** A General Data rule broken by one field; {@code field} uses the submission names, e.g. {@code phones/0/number}. */
public record GeneralDataViolation(String field, String reason) {}
