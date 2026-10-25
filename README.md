# Registro de proveedores — Fredkor Labs

Permite que personas físicas y morales inicien y continúen su registro como proveedores en la
Plataforma de Compras de Fredkor Labs: se identifican con e.firma, aceptan la Política de Aviso y
capturan su expediente por secciones (Datos Generales con teléfonos y cédula fiscal, y
participación con partidas del catálogo CUCoP). La primera etapa termina en el punto de
continuación hacia Contactos; el alcance completo está en la [visión](specs/vision.md).

## Cómo se trabaja: Spec-Driven Development

Las especificaciones de `specs/` definen el comportamiento y el código las implementa; no se
implementan reglas que no estén especificadas. El orden de lectura y las reglas obligatorias
(arquitectura hexagonal, contrato 1:1, errores, pruebas y commits) están en [CLAUDE.md](CLAUDE.md).

## Estructura del repositorio

| Carpeta | Contenido |
| :--- | :--- |
| [`specs/`](specs/) | Fuente de verdad del proyecto (detalle abajo). |
| [`backend/`](backend/) | API REST en Java 25 y Spring Boot 4.1, con arquitectura hexagonal ([ADR-0001](specs/adr/0001-backend-arquitectura-hexagonal.md)), PostgreSQL 17 y Flyway. Las interfaces y DTO web se generan desde el contrato OpenAPI. |
| [`frontend/`](frontend/) | Aplicación Angular 21 con PrimeNG y la plantilla Sakai ([ADR-0011](specs/adr/0011-frontend-angular-21.md), [ADR-0012](specs/adr/0012-frontend-primeng-sakai.md)). Hoy contiene la base de la plantilla; las pantallas del registro aún no están implementadas. |
| [`tools/`](tools/) | `spec_lint.py` (coherencia de specs y contrato, antes de implementar) y `spec_sync.py` (specs contra código y pruebas, después de implementar), con sus pruebas ([tools/README.md](tools/README.md)). |
| [`prompts/`](prompts/) | Instrucciones reutilizables para implementar operaciones, refactorizar desde el diseño y documentar. |
| `.claude/commands/` | Comandos de Claude Code que ejecutan esos prompts (`/implementar`, `/cambio-spec`, `/sincronizar`, etc.). |

### Contenido de `specs/`

| Ruta | Qué define |
| :--- | :--- |
| [`vision.md`](specs/vision.md) | Propósito, alcance de la primera etapa y lo que queda fuera. |
| [`requirements/`](specs/requirements/) | Requerimientos funcionales (RF-REG-01 a RF-REG-08), no funcionales y la Política de Aviso. |
| [`user-stories/`](specs/user-stories/) | Historias US01 a US11 con sus criterios de aceptación (CA-001, etc.). |
| [`domain/`](specs/domain/) | Ciclo de vida del expediente y catálogos con sus valores. |
| [`architecture/`](specs/architecture/) | Modelo de dominio (invariantes INV-01 a INV-39), arquitectura, diseño de base de datos y stack. |
| [`adr/`](specs/adr/README.md) | Decisiones de arquitectura (ADR-0001 en adelante). |
| [`api/`](specs/api/README.md) | Contrato OpenAPI, guía de API e integración con el componente de firma. |
| [`traceability.md`](specs/traceability.md) | Matriz que une operaciones, RF/HU, invariantes y verificación. |

## Cómo ejecutar el backend

Esta sección explica cómo levantar el backend en local y correr sus pruebas. Las decisiones de
fondo están en [ADR-0017](specs/adr/0017-backend-postgresql-persistencia-duradera.md) y
[ADR-0018](specs/adr/0018-backend-postgresql-en-pruebas-y-desarrollo.md).

### Requisitos

| Herramienta | Uso |
| :--- | :--- |
| Java 25 | Compilar y ejecutar el backend. Maven no hace falta instalarlo: se usa el wrapper `backend/mvnw`. |
| Docker | **Obligatorio.** Las pruebas levantan un PostgreSQL 17 desechable con Testcontainers, y en desarrollo local la base también corre en un contenedor. |
| Python 3 y PyYAML | Solo para las validaciones de `tools/` (`spec_lint.py` y `spec_sync.py`). |

Comprueba que Docker responde antes de empezar:

```bash
docker info
```

### 1. Base de datos local (PostgreSQL 17 en Docker)

La aplicación no trae una base embebida: necesita un PostgreSQL 17. Crea el contenedor una sola
vez; los datos persisten en el volumen `supplier-registration-data`.

```bash
docker run -d --name supplier-registration-db \
  -e POSTGRES_DB=supplier_registration \
  -e POSTGRES_USER=supplier \
  -e POSTGRES_PASSWORD='<elige-una-contraseña>' \
  -p 5433:5432 \
  -v supplier-registration-data:/var/lib/postgresql/data \
  postgres:17
```

- El puerto del host es `5433`, para no chocar con otro PostgreSQL local en `5432`.
- Las veces siguientes basta con `docker start supplier-registration-db`.
- No hace falta crear tablas: Flyway aplica las migraciones al arrancar la aplicación.

### 2. Variables de entorno

La configuración se lee de variables de entorno (`backend/src/main/resources/application.yaml`).
No guardes credenciales en el repositorio.

| Variable | Obligatoria | Descripción |
| :--- | :--- | :--- |
| `REGISTRATION_DB_URL` | Sí | URL JDBC, p. ej. `jdbc:postgresql://localhost:5433/supplier_registration`. |
| `REGISTRATION_DB_USERNAME` | Sí | Usuario de la base (`supplier` en el ejemplo). |
| `REGISTRATION_DB_PASSWORD` | Sí | Contraseña elegida al crear el contenedor. |
| `REGISTRATION_DOCUMENTS_ROOT` | En local, sí | Carpeta donde se guardan las cédulas cargadas (ADR-0010). Por omisión es `/repository`, que en una máquina de desarrollo normalmente no existe o no es escribible. |
| `REGISTRATION_JWT_PRIVATE_KEY_LOCATION` | No (sí con el perfil `prod`) | Llave privada RSA en PEM PKCS#8 (`-----BEGIN PRIVATE KEY-----`), p. ej. `file:/ruta/jwt-private.pem`. |
| `REGISTRATION_JWT_PUBLIC_KEY_LOCATION` | No (sí con el perfil `prod`) | Llave pública RSA en PEM (`-----BEGIN PUBLIC KEY-----`), p. ej. `file:/ruta/jwt-public.pem`. |

Sin llaves JWT la aplicación genera un par efímero y lo advierte en el log: los tokens emitidos
dejan de validar al reiniciar (ADR-0015, ADR-0018). Para conservarlos entre reinicios, genera un
par fuera del repositorio:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out ~/.registration/jwt-private.pem
openssl pkey -in ~/.registration/jwt-private.pem -pubout -out ~/.registration/jwt-public.pem
```

### 3. Ejecutar la aplicación

Desde `backend/`:

```bash
export REGISTRATION_DB_URL=jdbc:postgresql://localhost:5433/supplier_registration
export REGISTRATION_DB_USERNAME=supplier
export REGISTRATION_DB_PASSWORD='<tu-contraseña>'
export REGISTRATION_DOCUMENTS_ROOT="$HOME/.registration/documents"
# Opcional: llaves JWT persistentes
export REGISTRATION_JWT_PRIVATE_KEY_LOCATION="file:$HOME/.registration/jwt-private.pem"
export REGISTRATION_JWT_PUBLIC_KEY_LOCATION="file:$HOME/.registration/jwt-public.pem"

./mvnw spring-boot:run
```

También puedes pasarlas en la misma línea, sin `export`:

```bash
REGISTRATION_DB_URL=jdbc:postgresql://localhost:5433/supplier_registration \
REGISTRATION_DB_USERNAME=supplier \
REGISTRATION_DB_PASSWORD='<tu-contraseña>' \
REGISTRATION_DOCUMENTS_ROOT="$HOME/.registration/documents" \
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080/api/v1` y Swagger UI, con el contrato autoritativo
(ADR-0008), en `http://localhost:8080/swagger-ui/index.html`.

### 4. Pruebas

Desde `backend/`, con Docker en marcha:

```bash
./mvnw test
```

- No usan la base del paso 1 ni ninguna variable `REGISTRATION_DB_*`: cada ejecución levanta un
  PostgreSQL 17 nuevo en un puerto aleatorio, aplica las migraciones y lo destruye al terminar.
- Sin Docker disponible, las pruebas no pueden ejecutarse.

### 5. Validar specs y contrato

Desde la raíz del repositorio:

```bash
python3 tools/spec_lint.py --require-ready   # specs y contrato
python3 tools/spec_sync.py                   # specs contra código y pruebas
```

Para regenerar las interfaces y DTO desde el contrato: `./mvnw clean generate-sources` (desde
`backend/`). No edites `backend/target/generated-sources`.
