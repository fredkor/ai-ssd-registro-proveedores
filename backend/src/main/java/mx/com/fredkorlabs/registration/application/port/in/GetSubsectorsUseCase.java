package mx.com.fredkorlabs.registration.application.port.in;

public interface GetSubsectorsUseCase {
    CatalogOptionsResult execute(GetSubsectorsQuery query);
}
