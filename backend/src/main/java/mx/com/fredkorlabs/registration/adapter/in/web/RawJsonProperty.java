package mx.com.fredkorlabs.registration.adapter.in.web;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

/** Extracts the exact original text of a top-level JSON property value, without re-serializing it. */
final class RawJsonProperty {

    private RawJsonProperty() {
    }

    static String extract(JsonMapper jsonMapper, byte[] body, String propertyName) {
        try (JsonParser parser = jsonMapper.createParser(body)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("Request body is not a JSON object");
            }
            while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
                String name = parser.currentName();
                parser.nextToken();
                if (propertyName.equals(name)) {
                    int start = (int) parser.currentTokenLocation().getByteOffset();
                    parser.skipChildren();
                    int end = (int) parser.currentLocation().getByteOffset();
                    return new String(body, start, end - start, StandardCharsets.UTF_8);
                }
                parser.skipChildren();
            }
        }
        throw new IllegalArgumentException("Property not found: " + propertyName);
    }
}
