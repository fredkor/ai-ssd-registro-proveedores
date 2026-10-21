package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.RegistrationNoticesApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.AcceptRegistrationNoticeRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.NoticeAcceptanceResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.RegistrationNoticeResponse;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationNoticesController implements RegistrationNoticesApi {

    private final GetRegistrationNoticeUseCase getRegistrationNotice;

    public RegistrationNoticesController(GetRegistrationNoticeUseCase getRegistrationNotice) {
        this.getRegistrationNotice = getRegistrationNotice;
    }

    @Override
    public ResponseEntity<RegistrationNoticeResponse> getRegistrationNotice() {
        GetRegistrationNoticeResult result = getRegistrationNotice.execute();
        return ResponseEntity.ok(new RegistrationNoticeResponse(
            RegistrationNoticeResponse.VersionEnum._1_0,
            result.text()
        ));
    }

    @Override
    public ResponseEntity<NoticeAcceptanceResponse> acceptRegistrationNotice(
            AcceptRegistrationNoticeRequest acceptRegistrationNoticeRequest) {
        throw new UnsupportedOperationException("Not implemented in this iteration");
    }
}
