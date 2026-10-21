package mx.com.fredkorlabs.registration.adapter.in.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * Keeps the raw JSON body of generated request DTOs (needed to preserve ResultadoFirma exactly)
 * and enforces property presence before deserialization (ADR-0009).
 */
@ControllerAdvice
public class RawRequestBodyAdvice extends RequestBodyAdviceAdapter {

    static final String RAW_BODY_ATTRIBUTE = RawRequestBodyAdvice.class.getName() + ".rawBody";

    private final JsonMapper jsonMapper;

    public RawRequestBodyAdvice(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return RequestPropertyPresence.isGeneratedModel(targetType);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        byte[] body = inputMessage.getBody().readAllBytes();
        RequestContextHolder.currentRequestAttributes()
            .setAttribute(RAW_BODY_ATTRIBUTE, body, RequestAttributes.SCOPE_REQUEST);
        JsonNode tree = parseOrNull(body);
        if (tree != null) {
            RequestPropertyPresence.check(tree, (Class<?>) targetType, "");
        }
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body);
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }

    static byte[] currentRawBody() {
        return (byte[]) RequestContextHolder.currentRequestAttributes()
            .getAttribute(RAW_BODY_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    }

    private JsonNode parseOrNull(byte[] body) {
        if (body.length == 0) {
            return null;
        }
        try {
            return jsonMapper.readTree(body);
        } catch (JacksonException e) {
            return null; // malformed JSON is reported by the message converter
        }
    }
}
