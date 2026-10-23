package mx.com.fredkorlabs.registration.application.port.in;

public interface GetLegalOwnershipsUseCase {
    GetLegalOwnershipsResult execute(GetLegalOwnershipsQuery query);
}
