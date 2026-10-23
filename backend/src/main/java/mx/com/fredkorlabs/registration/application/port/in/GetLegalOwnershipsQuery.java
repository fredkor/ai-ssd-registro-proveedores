package mx.com.fredkorlabs.registration.application.port.in;

/** {@code origin} and {@code taxRegime} are the contract codes; {@code taxRegime} may be null. */
public record GetLegalOwnershipsQuery(String origin, String countryCode, String taxRegime) {}
