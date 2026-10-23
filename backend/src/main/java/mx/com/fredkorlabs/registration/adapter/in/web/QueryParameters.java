package mx.com.fredkorlabs.registration.adapter.in.web;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Set;

/** ADR-0020: a query parameter that the operation does not define, or a repeated one, is rejected. */
final class QueryParameters {

    private QueryParameters() {
    }

    static void requireOnly(Set<String> defined) {
        Map<String, String[]> received = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest().getParameterMap();
        received.forEach((name, values) -> {
            if (!defined.contains(name)) {
                throw new InvalidRequestParameterException(name, "is not a defined parameter");
            }
            if (values.length > 1) {
                throw new InvalidRequestParameterException(name, "must not be repeated");
            }
        });
    }
}
