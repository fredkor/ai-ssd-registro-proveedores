# Validación documental del registro

Ejecutar desde `SSD_REAL/`, con Python 3 y PyYAML disponibles:

```bash
python3 tools/spec_lint.py
python3 -m unittest discover -s tools -p 'test_*.py' -v
```

El script localiza el proyecto a partir de su propia ruta, por lo que también puede ejecutarse
desde otra carpeta. Consulta Maven y los enums Java en `backend/`, y el contrato
autoritativo en `specs/api/`. Para validar una copia explícita:

```bash
python3 tools/spec_lint.py --root /ruta/a/SSD_REAL
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
