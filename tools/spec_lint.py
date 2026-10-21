#!/usr/bin/env python3
"""Coherencia documental SDD del registro. No sustituye OpenAPI ni pruebas de ejecución.
Uso: python3 tools/spec_lint.py [--root RUTA] [--require-ready]. Requiere PyYAML.
No genera codigo, no modifica especificaciones y no valida SLAs ni sintaxis Mermaid.
"""
import argparse
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
import yaml

ROOT = Path(__file__).resolve().parent.parent
CONTRACT = ROOT / 'specs/api/supplier-registration-openapi.yaml'
METHODS = {'get', 'post', 'put', 'patch', 'delete', 'head', 'options', 'trace'}

class UniqueLoader(yaml.SafeLoader):
    pass

def unique_mapping(loader, node, deep=False):
    result = {}
    for key_node, value_node in node.value:
        key = loader.construct_object(key_node, deep=deep)
        if key in result:
            raise yaml.constructor.ConstructorError(None, None, f'Clave duplicada: {key}', key_node.start_mark)
        result[key] = loader.construct_object(value_node, deep=deep)
    return result

UniqueLoader.add_constructor(yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, unique_mapping)

def resolve(doc, ref):
    if not ref.startswith('#/'):
        raise ValueError(f'Referencia no local: {ref}')
    node = doc
    for key in ref[2:].split('/'):
        node = node[key.replace('~1', '/').replace('~0', '~')]
    return node

def walk(node, path='#'):
    if isinstance(node, dict):
        yield path, node
        for key, value in node.items():
            yield from walk(value, f'{path}/{key}')
    elif isinstance(node, list):
        for index, value in enumerate(node):
            yield from walk(value, f'{path}/{index}')

def tables(path):
    for line in path.read_text(encoding='utf-8').splitlines():
        if line.startswith('|'):
            yield [cell.strip() for cell in line.strip('|').split('|')]

def codes(cell):
    return re.findall(r'`([A-Z][A-Z0-9_]*)`', cell)

def check_structure(doc):
    """Comprobaciones estructurales necesarias para informar fallos sin traceback.

    No pretende sustituir un validador completo de OpenAPI.
    """
    errors = []
    paths = doc.get('paths')
    components = doc.get('components')
    if not isinstance(paths, dict) or not paths:
        errors.append('[OAS] paths debe ser un objeto no vacio')
    if not isinstance(components, dict) or not isinstance(components.get('schemas'), dict):
        errors.append('[OAS] components.schemas debe ser un objeto')
    if errors:
        return errors
    if not isinstance(doc.get('tags', []), list) or any(
            not isinstance(tag, dict) or not isinstance(tag.get('name'), str)
            for tag in doc.get('tags', [])):
        errors.append('[OAS] tags debe contener objetos con name')
    for path, item in paths.items():
        if not isinstance(path, str) or not path.startswith('/') or not isinstance(item, dict):
            errors.append(f'[OAS] Ruta invalida: {path}')
            continue
        for method, op in item.items():
            if method not in METHODS:
                continue
            if not isinstance(op, dict):
                errors.append(f'[OAS] {method.upper()} {path}: operacion debe ser un objeto')
                continue
            if not isinstance(op.get('responses'), dict):
                errors.append(f'[OAS] {method.upper()} {path}: responses debe ser un objeto')
            security = op.get('security', doc.get('security', []))
            if not isinstance(security, list) or any(not isinstance(v, dict) for v in security):
                errors.append(f'[OAS] {path}: security debe ser una lista de objetos')
            if isinstance(op.get('responses'), dict) and any(
                    not isinstance(value, dict) for value in op['responses'].values()):
                errors.append(f'[OAS] {path}: cada respuesta debe ser un objeto')
            for field in ('tags', 'x-spec-refs', 'x-pending-decisions'):
                if field in op and (not isinstance(op[field], list) or
                                   any(not isinstance(value, str) for value in op[field])):
                    errors.append(f'[OAS] {path}: {field} debe ser una lista de textos')
    return errors


def check_registration_rules(doc, root):
    """Reglas de frontera aprobadas del registro, no comportamiento de implementacion."""
    errors = []
    specs = root / 'specs'
    schemas = doc['components']['schemas']
    operations = [(path, method, op) for path, item in doc['paths'].items()
                  for method, op in item.items() if method in METHODS]
    public = {'getRegistrationNotice', 'acceptRegistrationNotice', 'identifyApplicant'}
    error_status = {code: row[0] for row in tables(specs/'adr/0004-shared-codigos-de-error.md')
                    if row[0].isdigit() for code in codes(row[1])}

    for path, method, op in operations:
        where = f'{method.upper()} {path}'
        security = op.get('security', doc.get('security', []))
        protected = any('RegistrationToken' in entry for entry in security)
        if op.get('operationId') not in public and not protected:
            errors.append(f'[AUTH] {where}: operacion del registro sin JWT')
        if op.get('operationId') in public and protected:
            errors.append(f'[AUTH] {where}: requiere un JWT previo antes de identificar')
        for status, response in op['responses'].items():
            if not str(status).startswith(('4', '5')):
                continue
            response = resolve(doc, response['$ref']) if '$ref' in response else response
            declared = response.get('x-error-codes', [])
            if not declared:
                errors.append(f'[ERROR] {where}: respuesta {status} sin x-error-codes')
            for code in declared:
                if error_status.get(code) != str(status):
                    errors.append(f'[ERROR] {where}: {status}/{code} contradice ADR-0004')

    # Restricciones documentales explicitas de US10, sin interpretar prosa como implementacion.
    uploads = [op for _, _, op in operations if op.get('operationId') == 'uploadFiscalDocument']
    if len(uploads) != 1:
        errors.append('[FILE] Debe existir una operacion uploadFiscalDocument')
    else:
        upload = uploads[0]
        media = upload.get('requestBody', {}).get('content', {}).get('multipart/form-data', {})
        file = media.get('schema', {}).get('properties', {}).get('file', {})
        if file.get('type') != 'string' or file.get('format') != 'binary':
            errors.append('[FILE] La carga debe recibir un archivo binario multipart')
        if file.get('x-max-size-bytes') != 5242880:
            errors.append('[FILE] Cedula: el maximo es 5 MiB, 5242880 bytes (US10)')
        if media.get('encoding', {}).get('file', {}).get('contentType') != 'application/pdf':
            errors.append('[FILE] Cedula: contentType debe ser application/pdf')
        if not {'413', '415'} <= upload.get('responses', {}).keys():
            errors.append('[FILE] Carga sin errores 413/415 de tamano/formato')
    general = schemas.get('GeneralDataRequest', {}).get('properties', {})
    if 'fiscalDocumentId' in general:
        errors.append('[FILE] Datos Generales valida la asociacion existente; no recibe fiscalDocumentId')
    deletes = [(method, op) for _, method, op in operations
               if op.get('operationId') == 'deleteFiscalDocument']
    if len(deletes) != 1 or deletes[0][0] != 'delete' or '204' not in deletes[0][1]['responses']:
        errors.append('[FILE] Eliminacion logica requiere DELETE y respuesta 204')

    # Aviso fijo y precisiones de campo que si se expresan en el contrato.
    notice = schemas.get('AcceptRegistrationNoticeRequest', {}).get('properties', {}).get('version', {})
    if notice.get('enum') != ['1.0']:
        errors.append('[NOTICE] El aviso fijo debe conservar version 1.0')
    website = general.get('website', {})
    if website.get('maxLength') != 2048 or website.get('nullable') is not True:
        errors.append('[FIELD] website requiere maxLength=2048 y nullable=true')
    phone = schemas.get('PhoneRequest', {}).get('properties', {})
    if phone.get('number', {}).get('pattern') != '^[0-9]{10}$':
        errors.append('[FIELD] El numero telefonico debe contener exactamente 10 digitos')
    if phone.get('extension', {}).get('maxLength') != 5 or phone.get('extension', {}).get('nullable') is not True:
        errors.append('[FIELD] Extension opcional: maxLength=5 y nullable=true')

    # Identificadores RNF: no se exige un endpoint por requisito no funcional.
    rnf_text = (specs/'requirements/no-functional.md').read_text(encoding='utf-8')
    rnf = re.findall(r'^## (RNF-\d{2}):', rnf_text, re.M)
    if not rnf or len(rnf) != len(set(rnf)):
        errors.append('[RNF] Requerimientos no funcionales ausentes o duplicados')
    valid_rnf = set(rnf)
    for source in specs.rglob('*.md'):
        for ref in set(re.findall(r'\bRNF-\d{2}\b', source.read_text(encoding='utf-8'))):
            if ref not in valid_rnf:
                errors.append(f'[RNF] {source.relative_to(root)}: referencia inexistente {ref}')
    return errors


def lint(doc, root=ROOT, require_ready=False):
    """Compara contrato con las fuentes locales; devuelve errores y advertencias sin escribir."""
    root = Path(root)
    specs = root / 'specs'
    backend = root / 'backend'
    errors, warnings = [], []
    def fail(rule, message):
        errors.append(f'[{rule}] {message}')
    if not isinstance(doc, dict) or doc.get('openapi') != '3.0.3':
        return ['[OAS] Se requiere OpenAPI 3.0.3'], []
    structure_errors = check_structure(doc)
    if structure_errors:
        return structure_errors, []
    for where, node in walk(doc):
        if '$ref' in node:
            try:
                resolve(doc, node['$ref'])
            except (KeyError, TypeError, ValueError, AttributeError):
                fail('REF', f'{where}: referencia inválida {node["$ref"]}')
            if len(node) > 1:
                fail('REF', f'{where}: usar allOf para propiedades junto a $ref')
    if errors:
        return errors, warnings
    schemas = doc.get('components', {}).get('schemas', {})
    rf = set(re.findall(r'^## (RF-REG-\d{2}):', (specs/'requirements/functional.md').read_text(encoding='utf-8'), re.M))
    stories = {p.name.split('-')[0]: p for p in (specs/'user-stories').glob('US*.md')}
    trace = (specs/'traceability.md').read_text(encoding='utf-8')
    arch = (specs/'architecture/architecture.md').read_text(encoding='utf-8')
    inv = re.findall(r'^\| (INV-\d{2}) \|', (specs/'architecture/domain-model.md').read_text(encoding='utf-8'), re.M)
    if inv != [f'INV-{n:02}' for n in range(1,40)]:
        fail('INV', 'Se requieren INV-01 a INV-39 únicos y en orden')
    declared_pending = {v['id'] for v in doc.get('x-pending-decisions', [])}
    for where, node in walk(doc):
        if where == '#':
            continue
        for decision in node.get('x-pending-decisions', []):
            if decision not in declared_pending:
                fail('PENDING', f'{where}: decisión no declarada {decision}')
    ids, covered = set(), set()
    tags = {t['name'] for t in doc.get('tags', [])}
    for path, item in doc.get('paths', {}).items():
        for method, op in item.items():
            if method not in METHODS:
                continue
            where = f'{method.upper()} {path}'
            oid = op.get('operationId')
            if not oid or oid in ids:
                fail('OP', f'{where}: operationId ausente o duplicado')
            ids.add(oid)
            if not op.get('summary') or not op.get('tags') or set(op['tags'])-tags:
                fail('OP', f'{where}: summary/tags inválidos')
            refs = set(op.get('x-spec-refs', []))
            if not refs or refs-(rf|stories.keys()):
                fail('TRACE', f'{where}: referencias RF/US ausentes o inexistentes')
            covered |= refs
            if not op.get('x-use-case') or f'`{op["x-use-case"]}`' not in arch:
                fail('TRACE', f'{where}: caso de uso no definido en arquitectura')
            if f'`{oid}`' not in trace:
                fail('TRACE', f'{where}: sin trazabilidad')
            params = item.get('parameters', [])+op.get('parameters', [])
            params = [resolve(doc,p['$ref']) if '$ref' in p else p for p in params]
            actual = {p['name'] for p in params if p.get('in')=='path' and p.get('required')}
            if actual != set(re.findall(r'\{([^}]+)\}',path)):
                fail('PARAM', f'{where}: parámetros de ruta inconsistentes')
            responses = op.get('responses',{})
            if 'requestBody' in op and '400' not in responses:
                fail('ERROR', f'{where}: body sin respuesta 400')
            security = op.get('security',doc.get('security',[]))
            protected = any('RegistrationToken' in entry for entry in security)
            if op.get('x-requires-accepted-identification') and not protected:
                fail('AUTH', f'{where}: identidad requerida sin token')
            if protected and '401' not in responses:
                fail('AUTH', f'{where}: protegida sin 401')
            for status,response in responses.items():
                if not str(status).startswith(('4','5')):
                    continue
                response = resolve(doc,response['$ref']) if '$ref' in response else response
                content = response.get('content',{})
                schema = content.get('application/problem+json',{}).get('schema',{})
                if list(content) != ['application/problem+json'] or schema.get('$ref') != '#/components/schemas/ProblemResponse':
                    fail('ERROR', f'{where}: {status} no usa Problem Details (ADR-0019)')
    missing = (rf|stories.keys())-covered-{'RF-REG-07','US07'}
    if missing:
        fail('TRACE', f'Primera etapa sin cobertura: {sorted(missing)}')
    if covered & {'RF-REG-07','US07'}:
        fail('SCOPE', 'Finalización futura no debe exponerse en esta etapa')
    trace_ids = {row[0].strip('`') for row in tables(specs/'traceability.md') if len(row)>1 and row[0].startswith('`') and '/api/' in row[1]}
    if trace_ids != ids:
        fail('TRACE', f'Operaciones matriz/contrato difieren: {trace_ids ^ ids}')
    for story,path in stories.items():
        criteria = re.findall(r'\*\*(CA-\d{3}):\*\*',path.read_text(encoding='utf-8'))
        match = re.search(rf'^### {story} —[^\n]*\n(.*?)(?=^### |^## |\Z)',trace,re.M|re.S)
        rows = re.findall(r'^\| (CA-\d{3}) \|',match[1],re.M) if match else []
        if len(criteria)!=len(set(criteria)) or sorted(rows)!=sorted(criteria):
            fail('CA', f'{story}: criterios duplicados o trazabilidad distinta')
    lifecycle = list(tables(specs/'domain/supplier-registration-lifecycle.md'))
    fixed = list(tables(specs/'domain/catalogs.md'))
    expected = {
        'RegistrationStatus':[c for row in lifecycle if row[0]=='Estado del expediente' for c in codes(row[1])],
        'SectionStatus':[c for row in lifecycle if row[0]=='Condición de sección' for c in codes(row[1])],
        'RegistrationSection':[c for row in lifecycle if row[0].isdigit() for c in codes(row[1])],
        'ErrorCode':[c for row in tables(specs/'adr/0004-shared-codigos-de-error.md') if row[0].isdigit() for c in codes(row[1])],
    }
    for name in ('TaxRegime','Origin','ParticipationType'):
        expected[name] = [c for row in fixed if row[0]==f'`{name}`' for c in codes(row[1])]
    for name,values in expected.items():
        if not values or schemas.get(name,{}).get('enum')!=values:
            fail('ENUM', f'{name}: contrato y dominio/ADR difieren')
        for java in (backend/'src/main/java').rglob(f'{name}.java'):
            if '/generated/' in java.as_posix():
                continue
            text = re.sub(r'/\*.*?\*/|//[^\n]*','',java.read_text(encoding='utf-8'),flags=re.S)
            match = re.search(r'enum\s+\w+\s*\{(.*?)(?:;|\})',text,re.S)
            actual = re.findall(r'(?:^|,)\s*([A-Z][A-Z0-9_]*)',match[1]) if match else []
            if actual!=values:
                fail('JAVA', f'{java.relative_to(root)}: enum distinto')
    jwt = doc.get('components',{}).get('securitySchemes',{}).get('RegistrationToken',{})
    if (jwt.get('type'),jwt.get('scheme'),jwt.get('bearerFormat'))!=('http','bearer','JWT'):
        fail('AUTH','Se requiere HTTP Bearer JWT')
    def request_schema(schema, seen=frozenset()):
        if '$ref' in schema:
            ref=schema['$ref']
            if ref in seen:
                return
            seen=seen|{ref};schema=resolve(doc,ref)
        if schema.get('type')=='object':
            props=schema.get('properties',{})
            if schema.get('additionalProperties') is not False or set(schema.get('required',[]))!=set(props):
                fail('REQUEST','Requests requieren todas las propiedades y additionalProperties=false')
            for value in props.values():
                request_schema(value,seen)
        if 'items' in schema:
            request_schema(schema['items'],seen)
        for sub in schema.get('allOf',[]):
            request_schema(sub,seen)
    for _,node in walk(doc.get('paths',{})):
        if 'requestBody' in node:
            for media in node['requestBody'].get('content',{}).values():
                request_schema(media.get('schema',{}))
    for path in [*specs.rglob('*.md'),root/'CLAUDE.md']:
        for link in re.findall(r'\]\(([^)]+)\)',path.read_text(encoding='utf-8')):
            if '://' not in link and not link.startswith('#') and not (path.parent/link.split('#')[0]).exists():
                fail('LINK',f'{path.relative_to(root)}: {link}')
    pom=ET.parse(backend/'pom.xml').getroot();ns={'m':'http://maven.apache.org/POM/4.0.0'}
    if pom.findtext('m:groupId',namespaces=ns)!='mx.com.fredkorlabs':
        fail('MAVEN','groupId debe ser mx.com.fredkorlabs')
    if pom.findtext('.//m:inputSpec',namespaces=ns)!='${project.basedir}/../specs/api/supplier-registration-openapi.yaml':
        fail('MAVEN','inputSpec no apunta al contrato del registro')
    for field,suffix in [('apiPackage',''),('modelPackage','.model')]:
        if pom.findtext(f'.//m:{field}',namespaces=ns)!=f'mx.com.fredkorlabs.registration.adapter.in.web.generated{suffix}':
            fail('MAVEN',f'{field} no usa el paquete del registro')
    ready=doc.get('x-implementation-ready') is True
    if ready and (declared_pending or any(op.get('x-implementation-ready') is not True for item in doc['paths'].values() for m,op in item.items() if m in METHODS)):
        fail('READY','Habilitación global contradice operaciones o pendientes')
    if not ready:
        (errors if require_ready else warnings).append('[READY] Borrador: coherencia no habilita implementación')
    if not (backend/'src/main/java').exists():
        warnings.append('[CODE] No hay implementación Java; no se verificó comportamiento ni compilación')
    errors.extend(check_registration_rules(doc, root))
    return errors,warnings

def main(argv=None):
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--require-ready',action='store_true')
    parser.add_argument('--root',type=Path,default=ROOT,help='Raiz de SSD_REAL; por defecto se usa la ubicacion del script')
    args=parser.parse_args(argv)
    try:
        root=args.root.resolve()
        contract=root/'specs/api/supplier-registration-openapi.yaml'
        doc=yaml.load(contract.read_text(encoding='utf-8'),Loader=UniqueLoader)
        errors,warnings=lint(doc,root=root,require_ready=args.require_ready)
    except (OSError,yaml.YAMLError,ET.ParseError) as exc:
        print(f'ERROR [INPUT] {exc}');return 1
    print(f'Proyecto: {root}')
    print(f'Contrato: {contract.relative_to(root)}')
    print(f'Errores: {len(errors)} | Advertencias: {len(warnings)}')
    for e in errors:print(f'  ERROR {e}')
    for w in warnings:print(f'  AVISO {w}')
    print('RESULTADO:', 'FALLO' if errors else 'OK — coherencia documental; no valida ejecución')
    return int(bool(errors))

if __name__=='__main__':
    sys.exit(main())
