package mx.com.fredkorlabs.registration.adapter.in.web;

import mx.com.fredkorlabs.registration.adapter.in.web.generated.model.GeneralDataRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/**
 * ADR-0009: editable texts are trimmed before any validation, also the DTO constraints; an empty result becomes
 * null. Identifiers, catalog codes, tokens, file names and the signature result are never touched.
 */
final class EditableTextFields {

    private static final String EACH_ITEM = "*";

    private static final Map<Class<?>, List<List<String>>> FIELDS = Map.of(
        GeneralDataRequest.class, List.of(
            List.of("nationality"),
            List.of("foreignFiscalCode"),
            List.of("website"),
            List.of("phones", EACH_ITEM, "number"),
            List.of("phones", EACH_ITEM, "extension")));

    private EditableTextFields() {
    }

    static boolean apply(Class<?> model) {
        return FIELDS.containsKey(model);
    }

    /** Normalizes the request tree in place; values that are not strings are left for deserialization to reject. */
    static void normalize(JsonNode tree, Class<?> model) {
        for (List<String> path : FIELDS.getOrDefault(model, List.of())) {
            normalize(tree, path);
        }
    }

    private static void normalize(JsonNode node, List<String> path) {
        if (node == null) {
            return;
        }
        String head = path.getFirst();
        List<String> rest = path.subList(1, path.size());
        if (EACH_ITEM.equals(head)) {
            if (node.isArray()) node.forEach(item -> normalize(item, rest));
            return;
        }
        if (!(node instanceof ObjectNode object)) {
            return;
        }
        if (!rest.isEmpty()) {
            normalize(object.get(head), rest);
            return;
        }
        JsonNode value = object.get(head);
        if (value != null && value.isString()) {
            String trimmed = value.asString().strip();
            if (trimmed.isEmpty()) {
                object.putNull(head);
            } else {
                object.put(head, trimmed);
            }
        }
    }
}
