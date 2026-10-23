-- ADR-0017/ADR-0020: catálogo de giros (catalog-database-proposal, "Catálogos sin relaciones de selección
-- entre sí"). Valores de specs/domain/catalog-values.md (catálogo 7) con sus IDs originales.

create table business_activity (
    id          integer      not null primary key,
    description varchar(500) not null
);

insert into business_activity (id, description) values
    (1, 'INDUSTRIAL'),
    (2, 'COMERCIAL'),
    (3, 'DE SERVICIOS');
