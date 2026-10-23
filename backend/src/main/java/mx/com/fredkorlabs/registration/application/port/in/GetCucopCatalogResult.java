package mx.com.fredkorlabs.registration.application.port.in;

import java.util.List;

/** The CUCoP tree: chapters at the root, each node with its children in {@code items}, siblings by key. */
public record GetCucopCatalogResult(List<CucopNode> items) {

    public GetCucopCatalogResult {
        items = List.copyOf(items);
    }

    /** {@code items} holds the children of the next level; it is empty for specific items (level 4). */
    public record CucopNode(int id, String key, String description, int level, List<CucopNode> items) {

        public CucopNode {
            items = List.copyOf(items);
        }
    }
}
