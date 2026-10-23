package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetCucopCatalogResult;
import mx.com.fredkorlabs.registration.application.port.in.GetCucopCatalogResult.CucopNode;
import mx.com.fredkorlabs.registration.application.port.in.GetCucopCatalogUseCase;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalog;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalogPort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetCucopCatalogService implements GetCucopCatalogUseCase {

    // Contract: siblings ordered by key (ids can repeat across levels).
    private static final Comparator<CucopNode> BY_KEY = Comparator.comparing(CucopNode::key);

    private final CucopCatalogPort cucopCatalog;

    public GetCucopCatalogService(CucopCatalogPort cucopCatalog) {
        this.cucopCatalog = cucopCatalog;
    }

    @Override
    public GetCucopCatalogResult execute() {
        CucopCatalog catalog = cucopCatalog.findCatalog();
        // Built bottom-up: each level groups its nodes under the parent id of the level above.
        Map<Integer, List<CucopNode>> specificsByGeneric = group(catalog.specificItems().stream()
            .collect(Collectors.groupingBy(specific -> specific.genericItemId(),
                Collectors.mapping(specific -> new CucopNode(specific.id(), specific.key(), specific.description(),
                    specific.level(), List.of()), Collectors.toList()))));
        Map<Integer, List<CucopNode>> genericsByConcept = group(catalog.genericItems().stream()
            .collect(Collectors.groupingBy(generic -> generic.conceptId(),
                Collectors.mapping(generic -> new CucopNode(generic.id(), generic.key(), generic.description(),
                    generic.level(), children(specificsByGeneric, generic.id())), Collectors.toList()))));
        Map<Integer, List<CucopNode>> conceptsByChapter = group(catalog.concepts().stream()
            .collect(Collectors.groupingBy(concept -> concept.chapterId(),
                Collectors.mapping(concept -> new CucopNode(concept.id(), concept.key(), concept.description(),
                    concept.level(), children(genericsByConcept, concept.id())), Collectors.toList()))));
        return new GetCucopCatalogResult(catalog.chapters().stream()
            .map(chapter -> new CucopNode(chapter.id(), chapter.key(), chapter.description(), chapter.level(),
                children(conceptsByChapter, chapter.id())))
            .sorted(BY_KEY)
            .toList());
    }

    private static Map<Integer, List<CucopNode>> group(Map<Integer, List<CucopNode>> byParent) {
        return byParent.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().stream().sorted(BY_KEY).toList()));
    }

    private static List<CucopNode> children(Map<Integer, List<CucopNode>> byParent, int parentId) {
        return byParent.getOrDefault(parentId, List.of());
    }
}
