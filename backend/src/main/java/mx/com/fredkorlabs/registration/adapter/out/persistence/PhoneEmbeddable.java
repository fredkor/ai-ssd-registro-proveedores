package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** One row of registration_phone; its technical position is the list index (design §2). */
@Embeddable
public class PhoneEmbeddable {

    @Column(name = "number", nullable = false, length = 10)
    private String number;

    @Column(name = "extension", length = 5)
    private String extension;

    protected PhoneEmbeddable() {
    }

    public PhoneEmbeddable(String number, String extension) {
        this.number = number;
        this.extension = extension;
    }

    public String getNumber() { return number; }

    public String getExtension() { return extension; }
}
