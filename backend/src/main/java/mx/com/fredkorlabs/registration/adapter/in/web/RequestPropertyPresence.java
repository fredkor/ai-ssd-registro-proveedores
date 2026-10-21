package mx.com.fredkorlabs.registration.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * ADR-0009: every property defined in a request schema must be sent, also in nested objects.
 * Generated DTOs cannot distinguish an omitted property from null, so presence is checked on the JSON tree.
 */
final class RequestPropertyPresence {

    static final String GENERATED_MODEL_PACKAGE = "mx.com.fredkorlabs.registration.adapter.in.web.generated.model";

    private RequestPropertyPresence() {
    }

    static boolean isGeneratedModel(Type type) {
        return type instanceof Class<?> clazz && !clazz.isEnum()
            && GENERATED_MODEL_PACKAGE.equals(clazz.getPackageName());
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
            List<String> propertyPath = append(path, name);
            if (!node.has(name)) {
                throw new MissingRequestPropertyException(propertyPath);
            }
            checkNested(node.get(name), getter.getGenericReturnType(), propertyPath);
        }
    }

    private static void checkNested(JsonNode value, Type type, List<String> path) {
        if (isGeneratedModel(type)) {
            check(value, (Class<?>) type, path);
        } else if (type instanceof ParameterizedType parameterized
            && parameterized.getRawType() instanceof Class<?> raw && List.class.isAssignableFrom(raw)
            && isGeneratedModel(parameterized.getActualTypeArguments()[0]) && value != null && value.isArray()) {
            for (int i = 0; i < value.size(); i++) {
                check(value.get(i), (Class<?>) parameterized.getActualTypeArguments()[0], append(path, String.valueOf(i)));
            }
        }
    }

    private static List<String> append(List<String> path, String segment) {
        List<String> extended = new ArrayList<>(path);
        extended.add(segment);
        return extended;
    }
}
