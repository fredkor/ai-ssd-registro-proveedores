# Prompt: Refactorizar según un cambio de diseño

Se usa después de actualizar la spec (commit `design:`) y antes del commit `refactor:`.
Variables: {{SPEC_REF}} = archivo y sección de la spec; {{CAMBIO}} = qué debe moverse o cambiar.

```
Refactoriza el código según el cambio de diseño en {{SPEC_REF}}.

Cambio a aplicar: {{CAMBIO}}

Es un REFACTOR PURO:
- No cambies comportamiento ni lo que verifican las pruebas; solo puedes ajustar cómo obtienen sus dependencias.
- Si para que una prueba pase necesitas cambiar lo que verifica, DETENTE: no es un refactor puro.
- No modifiques specs/ ni archivos fuera de este cambio.

Ejecuta ./mvnw test y python tools/spec_lint.py, y reporta el resultado y la lista de archivos modificados.
```

## Ejemplo de uso
- {{SPEC_REF}} = specs/architecture/architecture.md §3 (`adapter.out.persistence` depende solo de `application.port.out`, dominio y JPA)
- {{CAMBIO}} = Quita la dependencia de `SupplierRegistrationPersistenceAdapter` hacia `adapter.out.catalog.CucopSpecificItemJpaRepository`.