package mx.com.fredkorlabs.registration.domain.model;

public record CucopGenericItem(int id, int conceptId, String key, String description, int level) {
    public CucopGenericItem {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (description == null) throw new IllegalArgumentException("Description cannot be null");
    }
}
