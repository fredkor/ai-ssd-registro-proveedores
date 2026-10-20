"""Regresiones de inconsistencias que el control SDD debe rechazar."""
import copy
import unittest
import tempfile
import shutil
from pathlib import Path
import yaml
import spec_lint


class SpecLintTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.contract = yaml.load(spec_lint.CONTRACT.read_text(), Loader=spec_lint.UniqueLoader)

    def changed(self, mutate):
        doc = copy.deepcopy(self.contract)
        mutate(doc)
        return spec_lint.lint(doc)[0]

    def test_current_draft_passes_coherence_but_not_readiness(self):
        errors, warnings = spec_lint.lint(self.contract)
        self.assertEqual(errors, [])
        self.assertTrue(any('[READY]' in item for item in warnings))
        errors, _ = spec_lint.lint(self.contract, require_ready=True)
        self.assertTrue(any('[READY]' in item for item in errors))

    def test_dangling_reference_returns_diagnostic(self):
        errors = self.changed(lambda d: d['components']['schemas']['Rfc'].update({'$ref': '#/components/schemas/Missing'}))
        self.assertTrue(any('[REF]' in item for item in errors))

    def test_unknown_business_reference_is_rejected(self):
        errors = self.changed(lambda d: d['paths']['/api/v1/registration-notices/current']['get']['x-spec-refs'].append('RF-99'))
        self.assertTrue(any('[TRACE]' in item for item in errors))

    def test_future_finalization_cannot_satisfy_first_stage_coverage(self):
        errors = self.changed(lambda d: d['paths']['/api/v1/registration-notices/current']['get']['x-spec-refs'].append('US07'))
        self.assertTrue(any('[SCOPE]' in item for item in errors))

    def test_domain_enum_drift_is_rejected(self):
        errors = self.changed(lambda d: d['components']['schemas']['RegistrationStatus']['enum'].append('DRAFT'))
        self.assertTrue(any('[ENUM]' in item for item in errors))

    def test_undeclared_pending_decision_is_rejected(self):
        errors = self.changed(lambda d: d['components']['schemas']['GeneralDataRequest'].update({'x-pending-decisions': ['API-404']}))
        self.assertTrue(any('[PENDING]' in item for item in errors))

    def test_nested_unknown_or_omitted_request_fields_are_rejected(self):
        errors = self.changed(lambda d: d['components']['schemas']['PhoneRequest'].update({'additionalProperties': True}))
        self.assertTrue(any('[REQUEST]' in item for item in errors))
        errors = self.changed(lambda d: d['components']['schemas']['PhoneRequest']['required'].remove('extension'))
        self.assertTrue(any('[REQUEST]' in item for item in errors))

    def test_jwt_contract_cannot_silently_revert_to_opaque_bearer(self):
        errors = self.changed(lambda d: d['components']['securitySchemes']['RegistrationToken'].pop('bearerFormat'))
        self.assertTrue(any('[AUTH]' in item for item in errors))

    def test_duplicate_yaml_key_is_not_silently_overwritten(self):
        with self.assertRaises(yaml.YAMLError):
            yaml.load('openapi: 3.0.3\nopenapi: 3.1.0\n', Loader=spec_lint.UniqueLoader)

    def test_global_ready_cannot_override_draft_operations(self):
        errors = self.changed(lambda d: d.update({'x-implementation-ready': True}))
        self.assertTrue(any('[READY]' in item for item in errors))

    def test_required_jwt_cannot_be_removed_with_its_marker(self):
        def mutate(doc):
            op = doc['paths']['/api/v1/supplier-registrations/{registrationId}/fiscal-document']['delete']
            op['security'] = []
            op.pop('x-requires-accepted-identification', None)
        errors = self.changed(mutate)
        self.assertTrue(any('[AUTH]' in item for item in errors))

    def test_error_http_status_must_match_adr(self):
        def mutate(doc):
            op = doc['paths']['/api/v1/registration-notices/current']['get']
            op['responses']['400'] = {'$ref': '#/components/responses/AccessDenied'}
        errors = self.changed(mutate)
        self.assertTrue(any('[ERROR]' in item and 'ADR-0004' in item for item in errors))

    def test_upload_limit_cannot_drift_to_example_ten_mb(self):
        def mutate(doc):
            op = doc['paths']['/api/v1/supplier-registrations/{registrationId}/fiscal-document']['post']
            file = op['requestBody']['content']['multipart/form-data']['schema']['properties']['file']
            file['x-max-size-bytes'] = 10000000
        self.assertTrue(any('[FILE]' in item for item in self.changed(mutate)))

    def test_save_cannot_accept_file_uuid_for_deferred_association(self):
        def mutate(doc):
            schema = doc['components']['schemas']['GeneralDataRequest']
            schema['properties']['fiscalDocumentId'] = {'type': 'string', 'format': 'uuid'}
            schema['required'].append('fiscalDocumentId')
        self.assertTrue(any('[FILE]' in item for item in self.changed(mutate)))

    def test_fixed_notice_version_cannot_change_silently(self):
        errors = self.changed(lambda d: d['components']['schemas']['AcceptRegistrationNoticeRequest']['properties']['version'].update({'enum': ['2.0']}))
        self.assertTrue(any('[NOTICE]' in item for item in errors))

    def test_missing_operation_id_returns_errors_without_crashing(self):
        errors = self.changed(lambda d: d['paths']['/api/v1/registration-notices/current']['get'].pop('operationId'))
        self.assertTrue(any('[OP]' in item for item in errors))

    def test_malformed_path_item_has_structural_diagnostic(self):
        errors = self.changed(lambda d: d['paths'].update({'/invalid': None}))
        self.assertTrue(any('[OAS]' in item for item in errors))

    def test_duplicate_rnf_and_unknown_reference_are_reported(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            shutil.copytree(spec_lint.ROOT / 'specs', root / 'specs')
            for name in ['pom.xml', 'CLAUDE.md']:
                shutil.copyfile(spec_lint.ROOT / name, root / name)
            rnf = root / 'specs/requirements/no-functional.md'
            rnf.write_text(rnf.read_text(encoding='utf-8') + '\n## RNF-01: Duplicado\n\nReferencia RNF-99.\n', encoding='utf-8')
            errors, _ = spec_lint.lint(self.contract, root=root)
            self.assertTrue(any('[RNF]' in item and 'duplicados' in item for item in errors))
            self.assertTrue(any('[RNF]' in item and 'RNF-99' in item for item in errors))


if __name__ == '__main__':
    unittest.main()
