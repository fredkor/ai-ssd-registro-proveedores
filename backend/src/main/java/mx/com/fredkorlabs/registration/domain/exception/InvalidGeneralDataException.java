package mx.com.fredkorlabs.registration.domain.exception;

import mx.com.fredkorlabs.registration.domain.model.GeneralDataViolation;

import java.util.List;

/** Every General Data rule the submission breaks (INV-16 to INV-26), reported together. */
public class InvalidGeneralDataException extends RegistrationDomainException {

    private final List<GeneralDataViolation> violations;

    public InvalidGeneralDataException(List<GeneralDataViolation> violations) {
        super("General Data does not satisfy its rules");
        this.violations = List.copyOf(violations);
    }

    public List<GeneralDataViolation> violations() {
        return violations;
    }
}
