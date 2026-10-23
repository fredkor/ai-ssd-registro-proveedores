package mx.com.fredkorlabs.registration.application.port.out;

import mx.com.fredkorlabs.registration.domain.model.CucopChapter;
import mx.com.fredkorlabs.registration.domain.model.CucopConcept;
import mx.com.fredkorlabs.registration.domain.model.CucopGenericItem;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;

import java.util.List;

/** The four CUCoP levels as stored, in no particular order; each child holds its parent's id. */
public record CucopCatalog(
    List<CucopChapter> chapters,
    List<CucopConcept> concepts,
    List<CucopGenericItem> genericItems,
    List<CucopSpecificItem> specificItems
) {
    public CucopCatalog {
        chapters = List.copyOf(chapters);
        concepts = List.copyOf(concepts);
        genericItems = List.copyOf(genericItems);
        specificItems = List.copyOf(specificItems);
    }
}
