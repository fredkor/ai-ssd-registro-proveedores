package mx.com.fredkorlabs.registration.application.port.out;

public class NoticeAcceptanceAlreadyUsedException extends RuntimeException {
    public NoticeAcceptanceAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
