package mx.com.fredkorlabs.registration.application.port.in;

import java.util.UUID;

/** {@code registrationId} and {@code status} are present only when {@code exists} is true. */
public record CheckRegistrationExistenceResult(boolean exists, UUID registrationId, String status) {

    public static CheckRegistrationExistenceResult notFound() {
        return new CheckRegistrationExistenceResult(false, null, null);
    }
}
