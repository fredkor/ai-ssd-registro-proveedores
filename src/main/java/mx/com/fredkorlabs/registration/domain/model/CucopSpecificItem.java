package mx.com.fredkorlabs.registration.domain.model;

public record CucopSpecificItem(int id, int genericItemId, String key, String description, int level) {
    public CucopSpecificItem {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (description == null) throw new IllegalArgumentException("Description cannot be null");
    }
}
