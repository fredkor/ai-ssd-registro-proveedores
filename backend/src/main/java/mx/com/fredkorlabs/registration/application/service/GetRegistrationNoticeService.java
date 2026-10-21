package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeResult;
import mx.com.fredkorlabs.registration.application.port.in.GetRegistrationNoticeUseCase;

public class GetRegistrationNoticeService implements GetRegistrationNoticeUseCase {

    private static final String VERSION = "1.0";

    private static final String TEXT =
        "Al registrarse en el Sistema de Compras de \"Fredkor Labs\" MANIFIESTA SU CONFORMIDAD Y ACEPTACIÓN para:\n\n" +
        "Que los actos pertinentes al sistema, desde su inicio hasta su conclusión, se realicen a través de medios de comunicación electrónica usando su correo electrónico;\n\n" +
        "Que se dé por notificado de las actuaciones electrónicas que emita la empresa, en el mismo día en que consulta el Sistema de Compras de Fredkor Labs, y\n\n" +
        "Que en el supuesto de que por causas imputables a la empresa se encuentre imposibilitado para consultar el sistema, o abrir los documentos electrónicos que contengan la información depositada en dicho sistema, en los días quince y último de cada mes o bien, el día hábil siguiente si alguno de éstos fuera inhábil, lo hará del conocimiento de la empresa a más tardar dentro de los tres días hábiles siguientes a aquél en que ocurra dicho impedimento, por medios de comunicación electrónica.";

    @Override
    public GetRegistrationNoticeResult execute() {
        return new GetRegistrationNoticeResult(VERSION, TEXT);
    }
}
