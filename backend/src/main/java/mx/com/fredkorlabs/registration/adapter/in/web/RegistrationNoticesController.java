package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.RegistrationNoticesApi;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.AcceptRegistrationNoticeRequest;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.NoticeAcceptanceResponse;
import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.RegistrationNoticeResponse;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeCommand;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.AcceptRegistrationNoticeUseCase;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
public class RegistrationNoticesController implements RegistrationNoticesApi {

    private final GetRegistrationNoticeUseCase getRegistrationNotice;
    private final AcceptRegistrationNoticeUseCase acceptRegistrationNotice;

    public RegistrationNoticesController(GetRegistrationNoticeUseCase getRegistrationNotice,
                                         AcceptRegistrationNoticeUseCase acceptRegistrationNotice) {
        this.getRegistrationNotice = getRegistrationNotice;
        this.acceptRegistrationNotice = acceptRegistrationNotice;
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
        AcceptRegistrationNoticeResult result = acceptRegistrationNotice.execute(
            new AcceptRegistrationNoticeCommand(acceptRegistrationNoticeRequest.getVersion().getValue()));
        return ResponseEntity.ok(new NoticeAcceptanceResponse(
            NoticeAcceptanceResponse.VersionEnum.fromValue(result.version()),
            OffsetDateTime.ofInstant(result.acceptedAt(), ZoneOffset.UTC),
            result.acceptanceId()
        ));
    }
}
