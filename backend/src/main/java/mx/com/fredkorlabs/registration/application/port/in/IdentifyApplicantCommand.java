package mx.com.fredkorlabs.registration.application.port.in;

public record IdentifyApplicantCommand(String noticeAcceptanceId, SignatureResultInput signatureResult) {}
