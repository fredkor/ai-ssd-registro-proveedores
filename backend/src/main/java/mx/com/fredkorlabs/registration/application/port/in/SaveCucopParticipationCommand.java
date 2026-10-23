package mx.com.fredkorlabs.registration.application.port.in;

import java.util.List;
import java.util.UUID;

/** The whole CUCoP submission in request order; {@code participationTypes} are the contract codes. */
public record SaveCucopParticipationCommand(
    UUID registrationId,
    String registrationSessionId,
    List<String> participationTypes,
    List<Integer> specificItemIds
) {}
