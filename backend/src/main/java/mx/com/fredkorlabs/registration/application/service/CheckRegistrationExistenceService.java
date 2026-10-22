package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceQuery;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceResult;
import mx.com.fredkorlabs.registration.application.port.in.CheckRegistrationExistenceUseCase;
import mx.com.fredkorlabs.registration.application.port.out.SupplierRegistrationPersistencePort;
import mx.com.fredkorlabs.registration.domain.model.Rfc;

public class CheckRegistrationExistenceService implements CheckRegistrationExistenceUseCase {

    private final SupplierRegistrationPersistencePort supplierRegistrationPersistence;

    public CheckRegistrationExistenceService(SupplierRegistrationPersistencePort supplierRegistrationPersistence) {
        this.supplierRegistrationPersistence = supplierRegistrationPersistence;
    }

    @Override
    public CheckRegistrationExistenceResult execute(CheckRegistrationExistenceQuery query) {
        // A failing lookup propagates: it must never be reported as "does not exist" (INV-09, US02 CA-004).
        return supplierRegistrationPersistence.findStatusByRfc(new Rfc(query.rfc()))
            .map(view -> new CheckRegistrationExistenceResult(true, view.id().value(), view.status().name()))
            .orElseGet(CheckRegistrationExistenceResult::notFound);
    }
}
