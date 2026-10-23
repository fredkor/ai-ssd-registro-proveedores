package mx.com.fredkorlabs.registration.application.port.out;

/** Country catalog entry: {@code code} relates it to legal ownership; {@code name} is in Spanish. */
public record CountryRecord(int id, String code, String name) {}
