-- ADR-0017/ADR-0020: catálogo de estratificación (catalog-database-proposal, "Catálogos sin relaciones de
-- selección entre sí"). Valores de specs/domain/catalog-values.md (catálogo 8) con sus IDs originales;
-- Activo 1/0 se carga como true/false. La API expone solo los activos (domain/catalogs.md, Vigencia).

create table stratification (
    id          integer      not null primary key,
    description varchar(500) not null,
    active      boolean      not null
);

insert into stratification (id, description, active) values
    (1, 'COOPERATIVAS', true),
    (2, 'GRANDE', true),
    (3, 'MEDIANA', true),
    (4, 'MICRO', true),
    (5, 'NO MIPYME', false),
    (6, 'PEQUEÑA', true);
