package mx.com.fredkorlabs.registration.domain.model;

public record Username(String value) {
    public Username {
        if (value == null) throw new IllegalArgumentException("Username cannot be null");
    }
}
