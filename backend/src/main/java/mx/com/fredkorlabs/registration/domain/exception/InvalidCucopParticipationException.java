package mx.com.fredkorlabs.registration.domain.exception;

import mx.com.fredkorlabs.registration.domain.model.CucopParticipationViolation;

import java.util.List;

/** Every CUCoP rule the submission breaks (INV-31), reported together. */
public class InvalidCucopParticipationException extends RegistrationDomainException {

    private final List<CucopParticipationViolation> violations;

    public InvalidCucopParticipationException(List<CucopParticipationViolation> violations) {
        super("CUCoP participation does not satisfy its rules");
        this.violations = List.copyOf(violations);
    }

    public List<CucopParticipationViolation> violations() {
        return violations;
    }
}
