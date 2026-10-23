package mx.com.fredkorlabs.registration.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * uniqueItems arrays are generated as Sets, which would silently drop repeated items; they are rejected on the
 * JSON tree before deserialization instead (INV-31).
 */
final class RequestUniqueItems {

    private RequestUniqueItems() {
    }

    static void check(JsonNode node, Class<?> model, List<String> path) {
        if (node == null || !node.isObject()) {
            return; // type mismatches are reported by deserialization
        }
        for (Method getter : model.getMethods()) {
            JsonProperty property = getter.getAnnotation(JsonProperty.class);
            if (property == null || getter.getParameterCount() != 0 || Modifier.isStatic(getter.getModifiers())) {
                continue;
            }
            String name = property.value();
            JsonNode value = node.get(name);
            Type type = getter.getGenericReturnType();
            if (getter.getReturnType() == Set.class && value != null && value.isArray()) {
                rejectRepeated(value, append(path, name));
            } else if (RequestPropertyPresence.isGeneratedModel(type)) {
                check(value, (Class<?>) type, append(path, name));
            } else if (type instanceof ParameterizedType parameterized
                && parameterized.getRawType() == List.class
                && RequestPropertyPresence.isGeneratedModel(parameterized.getActualTypeArguments()[0])
                && value != null && value.isArray()) {
                for (int i = 0; i < value.size(); i++) {
                    check(value.get(i), (Class<?>) parameterized.getActualTypeArguments()[0],
                        append(append(path, name), String.valueOf(i)));
                }
            }
        }
    }

    private static void rejectRepeated(JsonNode array, List<String> path) {
        Set<JsonNode> seen = new HashSet<>();
        for (int i = 0; i < array.size(); i++) {
            if (!seen.add(array.get(i))) {
                throw new RepeatedRequestItemException(append(path, String.valueOf(i)));
            }
        }
    }

    private static List<String> append(List<String> path, String segment) {
        List<String> extended = new ArrayList<>(path);
        extended.add(segment);
        return extended;
    }
}
