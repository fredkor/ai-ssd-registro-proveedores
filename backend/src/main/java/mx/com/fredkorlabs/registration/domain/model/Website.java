package mx.com.fredkorlabs.registration.domain.model;

import mx.com.fredkorlabs.registration.domain.exception.InvalidWebsiteException;

import java.net.URI;
import java.net.URISyntaxException;

public record Website(String value) {
    public Website {
        if (value == null || value.isBlank()) {
            throw new InvalidWebsiteException("Website URL cannot be blank");
        }
        if (value.length() > 2048) {
            throw new InvalidWebsiteException("Website URL must not exceed 2048 characters");
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new InvalidWebsiteException(
                    "Website URL must use http or https scheme, got: " + scheme);
            }
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                throw new InvalidWebsiteException("Website URL must have a valid host");
            }
        } catch (URISyntaxException e) {
            throw new InvalidWebsiteException("Website URL is not a valid URI: " + e.getMessage());
        }
    }
}
