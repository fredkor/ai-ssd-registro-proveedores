# Validación documental del registro

Ejecutar desde la raíz del repositorio, con Python 3 y PyYAML disponibles:

```bash
python3 tools/spec_lint.py
python3 -m unittest discover -s tools -p 'test_*.py' -v
```

El script localiza el proyecto a partir de su propia ruta, por lo que también puede ejecutarse
desde otra carpeta. Consulta Maven y los enums Java en `backend/`, y el contrato
autoritativo en `specs/api/`. Para validar una copia explícita:

```bash
python3 tools/spec_lint.py --root /ruta/al/repositorio
```

## Controles

- YAML sin claves duplicadas y referencias internas resolubles.
- RF-REG, US/CA, INV, casos de uso y operaciones consistentes con la trazabilidad.
- Finalización futura excluida de los endpoints de primera etapa.
- Estados, secciones, tipos de participación y errores alineados con dominio y ADR.
- JWT requerido en operaciones protegidas; aviso, aceptación e identificación públicos.
- HTTP y `x-error-codes` de respuestas de error compatibles con ADR-0004.
- Presencia de propiedades y rechazo de desconocidas en requests y objetos anidados.
- Cédula PDF de 5 MiB, respuestas de tamaño/formato y operación DELETE de desasociación.
  Datos Generales no recibe un UUID editable para confirmar cargas diferidas.
- Versión fija del aviso, límites de campos telefónicos y página web.
- RNF sin IDs duplicados ni referencias a IDs inexistentes.
- Enlaces Markdown a archivos existentes y rutas/paquetes de generación de Maven.
- Enums Java de dominio, si existen; no compara DTO generados como clases de dominio.

## Resultado y alcance

Código de salida 0 significa que pasan estas comprobaciones documentales; 1 indica un fallo.
Un contrato en borrador se informa como advertencia, sin cambiar su estado. Para exigir además
la habilitación explícita del contrato:

```bash
python3 tools/spec_lint.py --require-ready
```

Con `x-implementation-ready: false`, ese comando devuelve 1 deliberadamente. No se puede
habilitar globalmente el contrato si sus operaciones siguen en borrador o quedan decisiones abiertas.

El lint no es un validador completo de OpenAPI o de ejemplos JSON, no renderiza Mermaid,
no comprueba anchors Markdown ni esquemas SQL, no verifica código de eliminación física/lógica,
no ejecuta pruebas del producto y no acredita cumplimiento de rendimiento, seguridad o disponibilidad.
Tampoco genera código, instala dependencias ni modifica especificaciones.

# Sincronización specs ↔ implementación

`spec_lint.py` valida las specs y el contrato **antes** de implementar. `spec_sync.py` verifica
**después** que el código y las pruebas de `backend/` siguen diciendo lo mismo que `specs/`:

```bash
python3 tools/spec_sync.py                 # informe en consola
python3 tools/spec_sync.py --md docs/sync-report.md   # además, informe en markdown
```

Código de salida 0 sin errores y 1 con errores; las advertencias no fallan. Acepta `--root` como
el lint. No compila ni ejecuta pruebas: eso lo hace `./mvnw test`.

| Regla | Comprueba | Fuente |
| :--- | :--- | :--- |
| SYNC-01 | Cada `operationId` lo implementa el controlador de su tag (`<Tag>Api`), sin TODO ni 501. | CLAUDE.md, regla 2. |
| SYNC-02 | Cada operación de `traceability.md` §2 tiene su `UseCase` en `port.in` y su `Service` en `application.service`, que lo implementa y se registra con `@Bean` en `configuration`. Contrato y matriz listan las mismas operaciones. | Arquitectura §5; CLAUDE.md, regla 3. |
| SYNC-03 | Las clases de prueba citadas en `traceability.md` existen. | Trazabilidad. |
| SYNC-04 | Cada CA tiene fila en `traceability.md` §3, y cada CA verificable en backend tiene una prueba `USxx_CAyyy_`. Un CA es de interfaz, externo o futuro cuando su operación es "Finalización futura" o su verificación empieza por "Interfaz", "Integración externa" o "Etapa posterior" sin nombrar dominio, aplicación, persistencia, web o backend. | CLAUDE.md, regla 7. |
| SYNC-05 | Ninguna prueba cita un CA inexistente. | Historias. |
| SYNC-06 | Las INV de `domain-model.md` y de `traceability.md` coinciden (los rangos "INV-30 a INV-37" se expanden). | Modelo de dominio. |
| SYNC-07 | El índice de ADR coincide con los archivos y ninguna spec, clase, `application.yaml`, prompt o comando cita un ADR inexistente. | `specs/adr/README.md`. |
| SYNC-08 | Los tipos que nombra `architecture.md` (UseCase, Service, Port, Adapter…) existen en `backend/src/main`. | Arquitectura. |
| SYNC-09 | Advertencia: tipos de `domain.model` que `domain-model.md` no describe. | Modelo de dominio. |
| SYNC-10 | Cada dependencia de `backend/pom.xml` está en `tech-stack.md` con la misma versión fija; una dependencia nueva o reemplazada por un ADR (H2) es error. | Stack; CLAUDE.md. |
| SYNC-11 | Cada `UseCase` de `port.in` y cada `Port` de `port.out` aparece en `architecture.md`. | Arquitectura §5 y §6. |
| SYNC-12 | Los `import` respetan la regla de dependencia: dominio y aplicación sin frameworks ni adaptadores; web sin `domain.model`, servicios, `port.out` ni adaptadores de salida; un adaptador de salida no importa la entrada, `port.in`, servicios ni otro adaptador de salida. | Arquitectura §3; ADR-0001. |

Una dependencia nueva se agrega a `POM_TO_STACK` en el mismo commit que su ADR y su fila de
`tech-stack.md`. El script no juzga si una prueba verifica de verdad su criterio, ni si un import
está justificado: eso lo revisa `/sincronizar`.
