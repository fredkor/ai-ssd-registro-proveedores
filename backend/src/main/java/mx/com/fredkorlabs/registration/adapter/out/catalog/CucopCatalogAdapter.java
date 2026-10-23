package mx.com.fredkorlabs.registration.adapter.out.catalog;

import mx.com.fredkorlabs.registration.application.port.out.CucopCatalog;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalogPort;
import mx.com.fredkorlabs.registration.domain.model.CucopChapter;
import mx.com.fredkorlabs.registration.domain.model.CucopConcept;
import mx.com.fredkorlabs.registration.domain.model.CucopGenericItem;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class CucopCatalogAdapter implements CucopCatalogPort {

    private final CucopChapterJpaRepository chapters;
    private final CucopConceptJpaRepository concepts;
    private final CucopGenericItemJpaRepository genericItems;
    private final CucopSpecificItemJpaRepository specificItems;

    public CucopCatalogAdapter(CucopChapterJpaRepository chapters, CucopConceptJpaRepository concepts,
                               CucopGenericItemJpaRepository genericItems,
                               CucopSpecificItemJpaRepository specificItems) {
        this.chapters = chapters;
        this.concepts = concepts;
        this.genericItems = genericItems;
        this.specificItems = specificItems;
    }

    @Override
    public CucopCatalog findCatalog() {
        return new CucopCatalog(
            chapters.findAll().stream()
                .map(chapter -> new CucopChapter(chapter.getId(), chapter.getKey(), chapter.getDescription(),
                    chapter.getLevel()))
                .toList(),
            concepts.findAll().stream()
                .map(concept -> new CucopConcept(concept.getId(), concept.getChapterId(), concept.getKey(),
                    concept.getDescription(), concept.getLevel()))
                .toList(),
            genericItems.findAll().stream()
                .map(generic -> new CucopGenericItem(generic.getId(), generic.getConceptId(), generic.getKey(),
                    generic.getDescription(), generic.getLevel()))
                .toList(),
            specificItems.findAll().stream()
                .map(specific -> new CucopSpecificItem(specific.getId(), specific.getGenericItemId(),
                    specific.getKey(), specific.getDescription(), specific.getLevel()))
                .toList());
    }

    @Override
    public List<CucopSpecificItem> findSpecificItems(Collection<Integer> ids) {
        return specificItems.findAllById(ids).stream()
            .map(specific -> new CucopSpecificItem(specific.getId(), specific.getGenericItemId(),
                specific.getKey(), specific.getDescription(), specific.getLevel()))
            .toList();
    }
}
