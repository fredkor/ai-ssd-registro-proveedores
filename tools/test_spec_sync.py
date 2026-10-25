"""Regresiones de desvíos specs <-> implementación que spec_sync debe detectar."""
import shutil
import tempfile
import unittest
from pathlib import Path

import spec_sync

PACKAGE = Path("backend/src/main/java/mx/com/fredkorlabs/registration")
COPIED = ["specs", "backend/src/main/java", "backend/src/test/java", "backend/pom.xml", "CLAUDE.md"]


class SpecSyncTest(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp())
        for rel in COPIED:
            source, target = spec_sync.ROOT / rel, self.tmp / rel
            if source.is_dir():
                shutil.copytree(source, target)
            else:
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(source, target)

    def tearDown(self):
        shutil.rmtree(self.tmp)

    def edit(self, rel, old, new):
        path = self.tmp / rel
        text = path.read_text(encoding="utf-8")
        self.assertIn(old, text, f"{rel} ya no contiene el texto de la prueba")
        path.write_text(text.replace(old, new, 1), encoding="utf-8")

    def append(self, rel, text):
        path = self.tmp / rel
        path.write_text(path.read_text(encoding="utf-8") + text, encoding="utf-8")

    def findings(self, rule):
        report, _ = spec_sync.run(self.tmp)
        return [msg for r, msg in report.errors + report.warnings if r == rule]

    def test_current_project_has_no_operation_traceability_adr_or_stack_drift(self):
        report, stats = spec_sync.run(self.tmp)
        rules = {rule for rule, _ in report.errors}
        self.assertFalse(rules & {"SYNC-01", "SYNC-02", "SYNC-03", "SYNC-05", "SYNC-06", "SYNC-07",
                                  "SYNC-08", "SYNC-10", "SYNC-11"}, report.errors)
        self.assertEqual(stats.operations, 22)

    def test_operation_without_controller_method_is_reported(self):
        self.edit(PACKAGE / "adapter/in/web/RegistrationCatalogsController.java",
                  " getSectors(", " getSectorsRenamed(")
        self.assertTrue(any("getSectors:" in m for m in self.findings("SYNC-01")))

    def test_operation_missing_from_traceability_is_reported(self):
        path = self.tmp / "specs/traceability.md"
        lines = [l for l in path.read_text(encoding="utf-8").splitlines() if not l.startswith("| `getSectors`")]
        path.write_text("\n".join(lines) + "\n", encoding="utf-8")
        self.assertTrue(any("getSectors está en el contrato" in m for m in self.findings("SYNC-02")))

    def test_service_not_registered_as_bean_is_reported(self):
        self.edit(PACKAGE / "configuration/RegistrationCatalogConfiguration.java",
                  "new GetSectorsService(", "newGetSectorsService(")
        self.assertTrue(any("GetSectorsService no se registra" in m for m in self.findings("SYNC-02")))

    def test_backend_criterion_without_test_is_reported_but_interface_one_is_not(self):
        self.append("specs/user-stories/US02-verificar-existencia-del-registro.md",
                    "\n- **CA-098:** Criterio de backend.\n- **CA-099:** Criterio de interfaz.\n")
        self.edit("specs/traceability.md", "\n### US03",
                  "\n| CA-098 | `checkRegistrationExistence` | INV-09 | Aplicación y persistencia. |"
                  "\n| CA-099 | Sin petición propia | INV-09 | Interfaz. |\n\n### US03")
        findings = self.findings("SYNC-04")
        self.assertTrue(any(m.startswith("US02_CA098") for m in findings))
        self.assertFalse(any(m.startswith("US02_CA099") for m in findings))

    def test_test_citing_unknown_criterion_is_reported(self):
        self.edit("backend/src/test/java/mx/com/fredkorlabs/registration/GetSectorsIntegrationTest.java",
                  "void ADR0015_withoutToken_returns401", "void US02_CA099_unknownCriterion")
        self.assertTrue(any(m.startswith("US02_CA099") for m in self.findings("SYNC-05")))

    def test_unknown_adr_reference_is_reported(self):
        self.append("CLAUDE.md", "\nVer ADR-0999.\n")
        self.assertTrue(any("ADR-0999" in m for m in self.findings("SYNC-07")))

    def test_undocumented_or_replaced_dependency_is_reported(self):
        self.edit("backend/pom.xml", "<dependencies>",
                  "<dependencies><dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId>"
                  "</dependency><dependency><groupId>x</groupId><artifactId>new-library</artifactId>"
                  "</dependency>")
        findings = self.findings("SYNC-10")
        self.assertTrue(any("h2" in m and "ADR-0018" in m for m in findings))
        self.assertTrue(any("new-library" in m for m in findings))

    def test_version_drift_against_tech_stack_is_reported(self):
        self.edit("backend/pom.xml", "<version>3.1.1</version>", "<version>3.2.0</version>")
        self.assertTrue(any("3.2.0" in m for m in self.findings("SYNC-10")))

    def test_framework_import_in_domain_is_reported(self):
        self.edit(PACKAGE / "domain/model/SectionStatus.java", "public enum",
                  "import org.springframework.stereotype.Component;\n\npublic enum")
        self.assertTrue(any("SectionStatus" in m for m in self.findings("SYNC-12")))

    def test_web_importing_domain_model_is_reported(self):
        self.edit(PACKAGE / "adapter/in/web/RegistrationCatalogsController.java", "import ",
                  "import mx.com.fredkorlabs.registration.domain.model.SectionStatus;\nimport ")
        self.assertTrue(any("RegistrationCatalogsController" in m for m in self.findings("SYNC-12")))

    def test_use_case_missing_from_architecture_is_reported(self):
        self.edit("specs/architecture/architecture.md", "GetSectorsUseCase", "GetSectorUseCase")
        self.assertTrue(any("GetSectorsUseCase" in m for m in self.findings("SYNC-11")))

    def test_invariant_ranges_are_expanded(self):
        self.assertEqual(spec_sync.expand_invariants("INV-30 a INV-32; INV-37"),
                         {"INV-30", "INV-31", "INV-32", "INV-37"})

    def test_backend_scope_follows_traceability_verification(self):
        self.assertFalse(spec_sync.backend_verifiable(("Sin petición propia", "Interfaz.")))
        self.assertFalse(spec_sync.backend_verifiable(("Finalización futura", "Dominio; validar secciones")))
        self.assertTrue(spec_sync.backend_verifiable(("`saveGeneralData`", "Interfaz, dominio y web: fila")))
        self.assertTrue(spec_sync.backend_verifiable(("Sin petición propia", "Aplicación y persistencia.")))


if __name__ == "__main__":
    unittest.main()
