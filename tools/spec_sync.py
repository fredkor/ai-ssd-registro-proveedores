#!/usr/bin/env python3
"""
spec_sync.py - Sincronización specs <-> implementación del Registro de proveedores.

Complementa a spec_lint.py: spec_lint valida el contrato ANTES de implementar; spec_sync verifica
DESPUÉS de implementar que el código, las pruebas y la documentación siguen diciendo lo mismo que specs/.

Uso:
    python3 tools/spec_sync.py                # informe en consola
    python3 tools/spec_sync.py --md FILE      # además escribe el informe en markdown
    python3 tools/spec_sync.py --root RUTA    # valida otra copia del proyecto
Salida: código 0 si no hay errores, 1 si hay errores (las advertencias no fallan).
Requiere: PyYAML. No compila ni ejecuta pruebas: para eso está ./mvnw test.
"""
import argparse
import re
import sys
from pathlib import Path
from types import SimpleNamespace

import yaml

try:
    sys.stdout.reconfigure(encoding="utf-8")  # consola de Windows
except AttributeError:
    pass

ROOT = Path(__file__).resolve().parent.parent
BASE_PACKAGE = "mx.com.fredkorlabs.registration"

# Tipos del JDK, de Spring o de librerías que aparecen en las specs y no son clases del proyecto.
EXTERNAL_TYPES = {
    "Clock", "UUID", "Optional", "Instant", "List", "String", "ProblemDetail", "ResponseEntity",
    "JpaRepository", "PlatformTransactionManager", "TransactionTemplate",
}

# Dependencia de backend/pom.xml -> texto que debe aparecer en tech-stack.md.
# Una dependencia nueva que no esté aquí es un error: agregarla exige un ADR (CLAUDE.md).
POM_TO_STACK = {
    "spring-boot-starter-parent": "Spring Boot",
    "spring-boot-starter-webmvc": "Web MVC",
    "spring-boot-starter-validation": "Validation",
    "spring-boot-starter-data-jpa": "Data JPA",
    "spring-boot-starter-flyway": "Flyway",
    "flyway-database-postgresql": "Flyway",
    "postgresql": "PostgreSQL",
    "spring-boot-starter-oauth2-resource-server": "OAuth2 Resource Server",
    "springdoc-openapi-starter-webmvc-ui": "springdoc-openapi-starter-webmvc-ui",
    "openapi-generator-maven-plugin": "openapi-generator-maven-plugin",
    "spring-boot-maven-plugin": "Maven",
    "spring-boot-starter-data-jpa-test": "starters de prueba",
    "spring-boot-starter-validation-test": "starters de prueba",
    "spring-boot-starter-webmvc-test": "starters de prueba",
    "spring-boot-testcontainers": "Testcontainers",
    "testcontainers-postgresql": "Testcontainers",
}
# Reemplazadas por un ADR: su presencia en el pom es un error.
FORBIDDEN_ARTIFACTS = {"h2": "ADR-0018 reemplazó H2 por PostgreSQL en todos los entornos"}

# CA que traceability.md asigna a la interfaz, a la integración externa o a una etapa posterior.
FRONTEND_PREFIXES = ("Interfaz", "Integración externa", "Etapa posterior")
BACKEND_WORDS = re.compile(r"\b(dominio|aplicaci[oó]n|persistencia|web|backend)\b", re.IGNORECASE)

# Regla de dependencia de architecture.md §3: paquete -> (import prohibido, motivo).
FRAMEWORKS = r"(org\.springframework|jakarta|org\.hibernate|tools\.jackson|com\.fasterxml)\."
LAYER_RULES = [
    ("domain", FRAMEWORKS, "el dominio es Java puro"),
    ("domain", rf"{BASE_PACKAGE}\.(application|adapter|configuration)\.", "el dominio no conoce otras capas"),
    ("application", FRAMEWORKS, "la aplicación es Java puro"),
    ("application", rf"{BASE_PACKAGE}\.(adapter|configuration)\.", "la aplicación no conoce adaptadores"),
    ("adapter.in.web", rf"{BASE_PACKAGE}\.domain\.model\.", "web no usa agregados ni valores de dominio"),
    ("adapter.in.web", rf"{BASE_PACKAGE}\.application\.(service|port\.out)\.", "web solo conoce application.port.in"),
    ("adapter.in.web", rf"{BASE_PACKAGE}\.adapter\.out\.", "web no conoce adaptadores de salida"),
    ("adapter.in.transaction", rf"{BASE_PACKAGE}\.adapter\.(out|in\.web)\.", "el decorador solo conoce port.in"),
    ("adapter.out", rf"{BASE_PACKAGE}\.adapter\.in\.", "un adaptador de salida no conoce la entrada"),
    ("adapter.out", rf"{BASE_PACKAGE}\.application\.(service|port\.in)\.", "un adaptador de salida solo conoce port.out"),
]


def project(root):
    root = Path(root).resolve()
    backend = root / "backend"
    return SimpleNamespace(
        root=root,
        specs=root / "specs",
        main=backend / "src" / "main" / "java",
        test=backend / "src" / "test" / "java",
        resources=backend / "src" / "main" / "resources",
        pom=backend / "pom.xml",
        contract=root / "specs" / "api" / "supplier-registration-openapi.yaml",
    )


class Report:
    def __init__(self):
        self.errors, self.warnings = [], []

    def error(self, rule, msg):
        self.errors.append((rule, msg))

    def warn(self, rule, msg):
        self.warnings.append((rule, msg))


# ------------------------------------------------------------------ lectura
def read(path):
    return path.read_text(encoding="utf-8", errors="replace")


def md_tables(path):
    """Tablas del markdown como lista de (encabezados, filas)."""
    tables, current = [], None
    for line in read(path).splitlines():
        line = line.strip()
        if not line.startswith("|"):
            current = None
            continue
        cells = [c.strip() for c in line.strip("|").split("|")]
        if current is None:
            current = (cells, [])
            tables.append(current)
        elif not all(set(c) <= set(":- ") for c in cells):
            current[1].append(cells)
    return tables


def java_files(base):
    return {p.stem: p for p in base.rglob("*.java")} if base.exists() else {}


def package_of(p, path):
    """Paquete relativo al base (p. ej. 'adapter.out.persistence')."""
    rel = path.relative_to(p.main / Path(*BASE_PACKAGE.split(".")))
    return ".".join(rel.parts[:-1])


def strip_code(cell):
    return cell.replace("`", "").strip()


def contract_operations(p):
    doc = yaml.safe_load(read(p.contract))
    return [(op["operationId"], (op.get("tags") or [""])[0]) for item in doc.get("paths", {}).values()
            for m, op in item.items() if isinstance(op, dict) and "operationId" in op]


def expand_invariants(text):
    """INV-xx citadas, con rangos 'INV-30 a INV-37' expandidos."""
    found = set()
    for first, last in re.findall(r"INV-(\d+)(?:\s+a\s+INV-(\d+))?", text):
        for n in range(int(first), int(last or first) + 1):
            found.add(f"INV-{n:02d}")
    return found


# ------------------------------------------------------------------ reglas
def check_operations(p, r, main):
    """SYNC-01: cada operationId lo implementa el controlador de su tag (CLAUDE.md, regla 2)."""
    controllers = {}
    for path in main.values():
        for m in re.finditer(r"class\s+\w+\s+implements\s+([\w\s,]+)\{", read(path)):
            for iface in (i.strip() for i in m.group(1).split(",")):
                controllers[iface] = path
    ops = contract_operations(p)
    for oid, tag in ops:
        ctrl = controllers.get(f"{tag}Api")
        if not ctrl:
            r.error("SYNC-01", f"{oid}: ningún controlador implementa {tag}Api")
            continue
        src = read(ctrl)
        m = re.search(rf"@Override\s+public\s+[^;{{]*?\b{oid}\s*\(", src)
        if not m:
            r.error("SYNC-01", f"{oid}: {ctrl.stem} no lo implementa (queda la respuesta 501 generada)")
            continue
        body = src[m.end():]
        nxt = re.search(r"\n\s*@Override", body)
        body = body[: nxt.start()] if nxt else body
        if re.search(r"UnsupportedOperationException|NOT_IMPLEMENTED|TODO", body):
            r.error("SYNC-01", f"{oid}: sigue como TODO / no implementado en {ctrl.stem}")
    return [oid for oid, _ in ops]


def check_traceability(p, r, main, test, op_ids):
    """SYNC-02: casos de uso y servicios de traceability.md §2 existen y están conectados.
    SYNC-03: clases de prueba citadas en traceability.md existen."""
    path = p.specs / "traceability.md"
    seen = set()
    configuration = "\n".join(read(f) for f in (p.main / Path(*BASE_PACKAGE.split("."))
                                                 / "configuration").rglob("*.java"))
    for headers, rows in md_tables(path):
        h = [x.lower() for x in headers]
        if "operationid" not in h or "caso de uso / servicio" not in h:
            continue
        for row in rows:
            cells = dict(zip(h, row))
            oid = strip_code(cells["operationid"])
            seen.add(oid)
            names = re.findall(r"`(\w+)`", cells["caso de uso / servicio"])
            uc = next((n for n in names if n.endswith("UseCase")), None)
            svc = next((n for n in names if n.endswith("Service")), None)
            if not uc or not svc:
                r.error("SYNC-02", f"traceability.md ({oid}): falta el caso de uso o el servicio")
                continue
            if uc not in main:
                r.error("SYNC-02", f"traceability.md ({oid}): no existe {uc}.java")
            elif package_of(p, main[uc]) != "application.port.in":
                r.error("SYNC-02", f"{uc} no está en application.port.in")
            if svc not in main:
                r.error("SYNC-02", f"traceability.md ({oid}): no existe {svc}.java")
                continue
            if package_of(p, main[svc]) != "application.service":
                r.error("SYNC-02", f"{svc} no está en application.service")
            if not re.search(rf"implements[^{{]*\b{uc}\b", read(main[svc])):
                r.error("SYNC-02", f"{svc} no implementa {uc}")
            if not re.search(rf"new\s+{svc}\s*\(", configuration):
                r.error("SYNC-02", f"{svc} no se registra con @Bean en configuration")
    for oid in op_ids:
        if oid not in seen:
            r.error("SYNC-02", f"{oid} está en el contrato pero no en traceability.md §2")
    for oid in sorted(seen - set(op_ids)):
        r.error("SYNC-02", f"traceability.md §2 lista {oid}, que no está en el contrato")
    for name in sorted(set(re.findall(r"\b([A-Z]\w*Test)\b", read(path)))):
        if name not in test:
            r.error("SYNC-03", f"traceability.md cita la clase de prueba {name}, que no existe")


def acceptance_criteria(p):
    """CA de cada historia con su fila de traceability.md §3: {US01_CA001: (operación, verificación)}."""
    rows, current = {}, None
    for line in read(p.specs / "traceability.md").splitlines():
        m = re.match(r"###\s+(US\d+)\b", line)
        if m:
            current = m.group(1)
            continue
        m = re.match(r"\|\s*CA-(\d+)\s*\|([^|]*)\|([^|]*)\|([^|]*)\|", line)
        if m and current:
            rows[f"{current}_CA{m.group(1)}"] = (strip_code(m.group(2)), strip_code(m.group(4)))
    criteria = {}
    for story in sorted((p.specs / "user-stories").glob("US*.md")):
        us = re.match(r"US(\d+)", story.name).group(1)
        for ca in re.findall(r"\*\*CA-(\d+):\*\*", read(story)):
            criteria[f"US{us}_CA{ca}"] = rows.get(f"US{us}_CA{ca}")
    return criteria


def backend_verifiable(row):
    operation, verification = row
    if operation.startswith("Finalización futura"):
        return False
    scope = verification.split(":")[0]
    return not (verification.startswith(FRONTEND_PREFIXES) and not BACKEND_WORDS.search(scope))


def check_acceptance_tests(p, r, test):
    """SYNC-04: cada CA verificable en backend tiene prueba USxx_CAyyy_ (CLAUDE.md, regla 7).
    SYNC-05: ninguna prueba cita un CA inexistente."""
    criteria = acceptance_criteria(p)
    methods = {}
    for path in test.values():
        for name in re.findall(r"void\s+(US\d+_CA\d+)_\w+\s*\(", read(path)):
            methods.setdefault(name, set()).add(path.stem)
    backend = set()
    for ca, row in sorted(criteria.items()):
        if row is None:
            r.error("SYNC-04", f"{ca}: criterio sin fila en traceability.md §3")
            continue
        if backend_verifiable(row):
            backend.add(ca)
            if ca not in methods:
                r.error("SYNC-04", f"{ca}: verificable en backend ('{row[1][:60]}') y sin prueba {ca}_")
    for name in sorted(set(methods) - set(criteria)):
        r.error("SYNC-05", f"{name}: prueba que cita un criterio inexistente ({', '.join(sorted(methods[name]))})")
    return criteria, backend, methods


def check_invariants(p, r):
    """SYNC-06: invariantes de domain-model.md y traceability.md coinciden."""
    model = set(re.findall(r"^\|\s*(INV-\d+)\s*\|", read(p.specs / "architecture" / "domain-model.md"), re.M))
    trace = expand_invariants(read(p.specs / "traceability.md"))
    for inv in sorted(model - trace):
        r.warn("SYNC-06", f"{inv} está en domain-model.md pero traceability.md no lo cita")
    for inv in sorted(trace - model):
        r.error("SYNC-06", f"{inv} se cita en traceability.md pero no existe en domain-model.md")


def check_adrs(p, r):
    """SYNC-07: índice de ADR completo y referencias ADR-NNNN válidas en specs, código y documentación."""
    adr_dir = p.specs / "adr"
    files = {re.match(r"(\d{4})", f.name).group(1) for f in adr_dir.glob("[0-9][0-9][0-9][0-9]-*.md")}
    files.discard("0000")
    index = set(re.findall(r"\[(\d{4})\]", read(adr_dir / "README.md")))
    for n in sorted(files - index):
        r.error("SYNC-07", f"ADR-{n} existe pero no está en specs/adr/README.md")
    for n in sorted(index - files):
        r.error("SYNC-07", f"specs/adr/README.md lista ADR-{n}, pero el archivo no existe")
    sources = list(p.specs.rglob("*.md")) + list(p.main.rglob("*.java")) + list(p.test.rglob("*.java"))
    sources += [f for f in p.resources.glob("*.y*ml")] if p.resources.exists() else []
    for pattern in ("CLAUDE.md", "README.md", "prompts/*.md", ".claude/commands/*.md", "tools/*.md"):
        sources += list(p.root.glob(pattern))
    for path in sources:
        for n in sorted(set(re.findall(r"ADR-(\d{4})", read(path)))):
            if n not in files and n != "0000":
                r.error("SYNC-07", f"{path.relative_to(p.root)} cita ADR-{n}, que no existe")


ARCH_SUFFIXES = (r"(UseCase|Service|Command|Query|Result|Port|Adapter|Mapper|Entity|Exception|"
                 r"Controller|Configuration|Repository|Handler|Advice)$")


def check_architecture_types(p, r, main):
    """SYNC-08: los tipos que nombra architecture.md existen.
    SYNC-11: cada UseCase y Port del código aparece en architecture.md.
    SYNC-09: tipos de domain.model que domain-model.md no describe (advertencia). Los adaptadores,
    Commands y Results no se nombran en las specs por diseño (architecture.md §5)."""
    text = read(p.specs / "architecture" / "architecture.md")
    generated = set(yaml.safe_load(read(p.contract)).get("components", {}).get("schemas", {}))
    named = set(re.findall(r"\b([A-Z][a-z0-9]+(?:[A-Z][a-z0-9]*)+)\b", text))
    for name in sorted(named):
        if name in EXTERNAL_TYPES or name in generated or name in main:
            continue
        suffix = re.search(ARCH_SUFFIXES, name)
        if suffix and suffix.start() > 0:
            r.error("SYNC-08", f"architecture.md nombra {name}, pero no existe en backend/src/main")
    for name, path in sorted(main.items()):
        pkg = package_of(p, path)
        if (pkg == "application.port.in" and name.endswith("UseCase")) or \
                (pkg == "application.port.out" and name.endswith("Port")):
            if not re.search(rf"\b{name}\b", text):
                r.error("SYNC-11", f"{name} ({pkg}) no aparece en architecture.md")
    model_text = read(p.specs / "architecture" / "domain-model.md")
    for name, path in sorted(main.items()):
        if package_of(p, path) == "domain.model" and not re.search(rf"\b{name}\b", model_text):
            r.warn("SYNC-09", f"{name} (domain.model) no aparece en domain-model.md")


def check_stack(p, r):
    """SYNC-10: dependencias de backend/pom.xml documentadas en tech-stack.md, con la misma versión."""
    pom = read(p.pom)
    stack_text = read(p.specs / "architecture" / "tech-stack.md")
    stack = stack_text.lower()
    own = re.search(r"</parent>\s*<groupId>[^<]*</groupId>\s*<artifactId>([^<]+)</artifactId>", pom)
    for m in re.finditer(r"<artifactId>([^<]+)</artifactId>\s*(?:<version>([^<]+)</version>)?", pom):
        artifact, version = m.group(1).strip(), (m.group(2) or "").strip()
        if own and artifact == own.group(1):
            continue
        if artifact in FORBIDDEN_ARTIFACTS:
            r.error("SYNC-10", f"pom.xml usa {artifact}: {FORBIDDEN_ARTIFACTS[artifact]}")
            continue
        keyword = POM_TO_STACK.get(artifact)
        if keyword is None:
            r.error("SYNC-10", f"pom.xml usa {artifact}, que no está documentado (dependencia nueva: ADR y tech-stack.md)")
        elif keyword.lower() not in stack:
            r.error("SYNC-10", f"pom.xml usa {artifact}, pero tech-stack.md no menciona '{keyword}'")
        if version and "${" not in version and keyword and version not in stack_text:
            r.error("SYNC-10", f"pom.xml fija {artifact} {version}, pero tech-stack.md no menciona esa versión")
    for keyword in ("ArchUnit", "JaCoCo", "H2"):
        if re.search(rf"\b{keyword.lower()}\b", stack) and keyword.lower() not in pom.lower():
            r.warn("SYNC-10", f"tech-stack.md menciona {keyword}, pero no está en pom.xml")


def check_layers(p, r, main):
    """SYNC-12: los import respetan la regla de dependencia de architecture.md §3."""
    for name, path in sorted(main.items()):
        if "generated" in path.parts:
            continue
        pkg = package_of(p, path)
        own_adapter = ".".join(pkg.split(".")[:3]) if pkg.startswith("adapter.out.") else None
        for imported in re.findall(r"^import\s+(?:static\s+)?([\w.]+)", read(path), re.M):
            for layer, forbidden, reason in LAYER_RULES:
                if (pkg == layer or pkg.startswith(layer + ".")) and re.match(forbidden, imported):
                    r.error("SYNC-12", f"{name} ({pkg}) importa {imported}: {reason}")
            if own_adapter and imported.startswith(f"{BASE_PACKAGE}.adapter.out.") and \
                    not imported.startswith(f"{BASE_PACKAGE}.{own_adapter}."):
                r.error("SYNC-12", f"{name} ({pkg}) importa {imported}: un adaptador de salida no depende de otro")


# ------------------------------------------------------------------ informe
def run(root=ROOT):
    p, r = project(root), Report()
    main, test = java_files(p.main), java_files(p.test)
    op_ids = check_operations(p, r, main)
    check_traceability(p, r, main, test, op_ids)
    criteria, backend, methods = check_acceptance_tests(p, r, test)
    check_invariants(p, r)
    check_adrs(p, r)
    check_architecture_types(p, r, main)
    check_stack(p, r)
    check_layers(p, r, main)
    stats = SimpleNamespace(operations=len(op_ids), criteria=criteria, backend=backend, methods=methods,
                            tests=sum(len(re.findall(r"void\s+US\d+_CA\d+_", read(f))) for f in test.values()))
    return r, stats


def report_md(path, r, stats):
    lines = ["# Informe de sincronización specs ↔ implementación", "",
             "Generado por `python3 tools/spec_sync.py --md`. No editar a mano.", "",
             f"**Resultado:** {'OK' if not r.errors else 'CON ERRORES'} — "
             f"errores: {len(r.errors)}, advertencias: {len(r.warnings)}", "",
             f"Operaciones: {stats.operations} | Criterios: {len(stats.criteria)} "
             f"({len(stats.backend)} verificables en backend) | Pruebas USxx_CAyyy: {stats.tests}", ""]
    if r.errors or r.warnings:
        lines += ["| Tipo | Regla | Hallazgo |", "| :--- | :--- | :--- |"]
        lines += [f"| Error | {rule} | {msg} |" for rule, msg in r.errors]
        lines += [f"| Advertencia | {rule} | {msg} |" for rule, msg in r.warnings]
        lines.append("")
    lines += ["## Cobertura de criterios de aceptación", "",
              "| Criterio | Verificación | Pruebas | Clases |", "| :--- | :--- | :---: | :--- |"]
    for ca in sorted(stats.criteria):
        scope = "backend" if ca in stats.backend else "interfaz / externa / futura"
        classes = sorted(stats.methods.get(ca, ()))
        lines.append(f"| {ca} | {scope} | {len(classes)} | {', '.join(classes) or '—'} |")
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text("\n".join(lines) + "\n", encoding="utf-8")


def main(argv=None):
    parser = argparse.ArgumentParser(description="Sincronización specs <-> implementación del registro")
    parser.add_argument("--root", default=ROOT, help="raíz del proyecto (por omisión, la de este script)")
    parser.add_argument("--md", metavar="FILE", help="escribe además el informe en markdown")
    args = parser.parse_args(argv)
    r, stats = run(args.root)
    print(f"Operaciones: {stats.operations} | Criterios de aceptación: {len(stats.criteria)} "
          f"({len(stats.backend)} verificables en backend) | Pruebas USxx_CAyyy: {stats.tests}")
    for rule, msg in r.errors:
        print(f"ERROR [{rule}] {msg}")
    for rule, msg in r.warnings:
        print(f"AVISO [{rule}] {msg}")
    print(f"Errores: {len(r.errors)} | Advertencias: {len(r.warnings)}")
    print("RESULTADO:", "OK - specs e implementación sincronizadas" if not r.errors
          else "ERROR - specs e implementación divergen")
    if args.md:
        report_md(args.md, r, stats)
    return 1 if r.errors else 0


if __name__ == "__main__":
    sys.exit(main())
