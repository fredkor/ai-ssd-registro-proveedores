package mx.com.fredkorlabs.registration.domain.model;

public record CucopConcept(int id, int chapterId, String key, String description, int level) {
    public CucopConcept {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if (description == null) throw new IllegalArgumentException("Description cannot be null");
    }
}
