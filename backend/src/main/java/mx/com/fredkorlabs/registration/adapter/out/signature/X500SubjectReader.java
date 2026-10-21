package mx.com.fredkorlabs.registration.adapter.out.signature;

import mx.com.fredkorlabs.registration.domain.model.CertificateSubjectAttribute;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal DER reader for an X.500 Name (RDNSequence). The JDK does not expose subject attributes
 * with their OIDs in encoding order, which ADR-0016 needs to pick the first matching attribute.
 */
final class X500SubjectReader {

    private static final int SEQUENCE = 0x30;
    private static final int SET = 0x31;
    private static final int OBJECT_IDENTIFIER = 0x06;

    private final byte[] der;
    private int position;

    private X500SubjectReader(byte[] der) {
        this.der = der;
    }

    static List<CertificateSubjectAttribute> read(byte[] encodedName) {
        X500SubjectReader reader = new X500SubjectReader(encodedName);
        int nameEnd = reader.enter(SEQUENCE);
        List<CertificateSubjectAttribute> attributes = new ArrayList<>();
        while (reader.position < nameEnd) {
            int rdnEnd = reader.enter(SET);
            while (reader.position < rdnEnd) {
                int atvEnd = reader.enter(SEQUENCE);
                String oid = reader.readOid();
                String value = reader.readString();
                reader.expectAt(atvEnd);
                attributes.add(new CertificateSubjectAttribute(oid, value));
            }
            reader.expectAt(rdnEnd);
        }
        reader.expectAt(nameEnd);
        return attributes;
    }

    private int enter(int expectedTag) {
        int tag = nextByte();
        if (tag != expectedTag) throw new IllegalArgumentException("Unexpected DER tag " + tag);
        int length = readLength();
        return position + length;
    }

    private String readOid() {
        int end = enter(OBJECT_IDENTIFIER);
        StringBuilder oid = new StringBuilder();
        long component = 0;
        boolean first = true;
        while (position < end) {
            int b = nextByte();
            component = (component << 7) | (b & 0x7F);
            if ((b & 0x80) == 0) {
                if (first) {
                    long head = Math.min(component / 40, 2);
                    oid.append(head).append('.').append(component - head * 40);
                    first = false;
                } else {
                    oid.append('.').append(component);
                }
                component = 0;
            }
        }
        return oid.toString();
    }

    private String readString() {
        int tag = nextByte();
        int length = readLength();
        int start = position;
        position += length;
        if (position > der.length) throw new IllegalArgumentException("DER value exceeds input");
        Charset charset = switch (tag) {
            case 0x0C -> StandardCharsets.UTF_8;          // UTF8String
            case 0x13, 0x16, 0x12 -> StandardCharsets.US_ASCII; // Printable, IA5, Numeric
            case 0x14 -> StandardCharsets.ISO_8859_1;     // T61String (Teletex)
            case 0x1E -> StandardCharsets.UTF_16BE;       // BMPString
            case 0x1C -> Charset.forName("UTF-32BE");     // UniversalString
            default -> throw new IllegalArgumentException("Unsupported DER string tag " + tag);
        };
        return new String(der, start, length, charset);
    }

    private int readLength() {
        int first = nextByte();
        if ((first & 0x80) == 0) return first;
        int octets = first & 0x7F;
        if (octets == 0 || octets > 4) throw new IllegalArgumentException("Unsupported DER length");
        int length = 0;
        for (int i = 0; i < octets; i++) {
            length = (length << 8) | nextByte();
        }
        if (length < 0) throw new IllegalArgumentException("Invalid DER length");
        return length;
    }

    private int nextByte() {
        if (position >= der.length) throw new IllegalArgumentException("Truncated DER input");
        return der[position++] & 0xFF;
    }

    private void expectAt(int end) {
        if (position != end) throw new IllegalArgumentException("Malformed DER structure");
    }
}
