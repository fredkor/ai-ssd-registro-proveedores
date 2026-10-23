package mx.com.fredkorlabs.registration.application.service;

import mx.com.fredkorlabs.registration.application.port.in.GetCucopCatalogResult.CucopNode;
import mx.com.fredkorlabs.registration.application.port.out.CucopCatalog;
import mx.com.fredkorlabs.registration.domain.model.CucopChapter;
import mx.com.fredkorlabs.registration.domain.model.CucopConcept;
import mx.com.fredkorlabs.registration.domain.model.CucopGenericItem;
import mx.com.fredkorlabs.registration.domain.model.CucopSpecificItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetCucopCatalogServiceTest {

    // Stored order on purpose: ids do not follow the keys (chapter 1000 is id 6) and ids repeat across levels.
    private static final CucopCatalog CATALOG = new CucopCatalog(
        List.of(new CucopChapter(1, "2000", "MATERIALES Y SUMINISTROS", 1),
            new CucopChapter(6, "1000", "SERVICIOS PERSONALES", 1)),
        List.of(new CucopConcept(2, 1, "2200", "ALIMENTOS Y UTENSILIOS", 2),
            new CucopConcept(1, 1, "2100", "MATERIALES DE ADMINISTRACION", 2),
            new CucopConcept(38, 6, "1200", "REMUNERACIONES AL PERSONAL DE CARACTER TRANSITORIO", 2)),
        List.of(new CucopGenericItem(9, 2, "2210", "PRODUCTOS ALIMENTICIOS PARA PERSONAS", 3),
            new CucopGenericItem(1, 1, "2110", "MATERIALES, UTILES Y EQUIPOS MENORES DE OFICINA", 3),
            new CucopGenericItem(242, 38, "1220", "SUELDOS BASE AL PERSONAL EVENTUAL", 3)),
        List.of(new CucopSpecificItem(10, 9, "22102", "PRODUCTOS ALIMENTICIOS PARA PERSONAS DERIVADO", 4),
            new CucopSpecificItem(9, 9, "22101", "PRODUCTOS ALIMENTICIOS PARA EL EJERCITO", 4),
            new CucopSpecificItem(1, 1, "21101", "MATERIALES Y ÚTILES DE OFICINA", 4),
            new CucopSpecificItem(367, 242, "12201", "REMUNERACIONES AL PERSONAL EVENTUAL", 4)));

    private final GetCucopCatalogService service = new GetCucopCatalogService(() -> CATALOG);

    private static CucopNode leaf(int id, String key, String description) {
        return new CucopNode(id, key, description, 4, List.of());
    }

    @Test
    void US11_CA015_catalog_isNestedFromChaptersToSpecificItems() {
        assertEquals(List.of(
                new CucopNode(6, "1000", "SERVICIOS PERSONALES", 1, List.of(
                    new CucopNode(38, "1200", "REMUNERACIONES AL PERSONAL DE CARACTER TRANSITORIO", 2, List.of(
                        new CucopNode(242, "1220", "SUELDOS BASE AL PERSONAL EVENTUAL", 3, List.of(
                            leaf(367, "12201", "REMUNERACIONES AL PERSONAL EVENTUAL"))))))),
                new CucopNode(1, "2000", "MATERIALES Y SUMINISTROS", 1, List.of(
                    new CucopNode(1, "2100", "MATERIALES DE ADMINISTRACION", 2, List.of(
                        new CucopNode(1, "2110", "MATERIALES, UTILES Y EQUIPOS MENORES DE OFICINA", 3, List.of(
                            leaf(1, "21101", "MATERIALES Y ÚTILES DE OFICINA"))))),
                    new CucopNode(2, "2200", "ALIMENTOS Y UTENSILIOS", 2, List.of(
                        new CucopNode(9, "2210", "PRODUCTOS ALIMENTICIOS PARA PERSONAS", 3, List.of(
                            leaf(9, "22101", "PRODUCTOS ALIMENTICIOS PARA EL EJERCITO"),
                            leaf(10, "22102", "PRODUCTOS ALIMENTICIOS PARA PERSONAS DERIVADO")))))))),
            service.execute().items());
    }

    @Test
    void US11_CA015_siblings_areOrderedByKeyNotById() {
        List<CucopNode> chapters = service.execute().items();

        assertEquals(List.of("1000", "2000"), chapters.stream().map(CucopNode::key).toList());
        assertEquals(List.of("2100", "2200"), chapters.get(1).items().stream().map(CucopNode::key).toList());
    }

    @Test
    void ADR0020_catalogFailure_propagates() {
        GetCucopCatalogService failing = new GetCucopCatalogService(() -> {
            throw new IllegalStateException("Database unavailable");
        });

        assertThrows(IllegalStateException.class, failing::execute);
    }
}
