package mx.com.fredkorlabs.registration.domain.model;

public record CucopChapter(int id, String key, String description, int level) {
    public CucopChapter {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (description == null) throw new IllegalArgumentException("Description cannot be null");
    }
}
