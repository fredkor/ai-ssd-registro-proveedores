-- ADR-0017/ADR-0020: catálogo de actividad preponderante (catalog-database-proposal, "Catálogos sin
-- relaciones de selección entre sí"). Valores de specs/domain/catalog-values.md (catálogo 9) con sus IDs originales.

create table primary_activity (
    id          integer      not null primary key,
    description varchar(500) not null
);

insert into primary_activity (id, description) values
    (1, 'RECURSOS NATURALES'),
    (2, 'CONSTRUCCIÓN'),
    (3, 'FABRICACIÓN PRODUCCIÓN O ELABORACIÓN'),
    (4, 'COMERCIO AL POR MAYOR'),
    (5, 'TRANSPORTE'),
    (6, 'COMUNICACIÓN'),
    (7, 'SERVICIOS'),
    (8, 'COMERCIO AL POR MENOR');
