-- ADR-0017/ADR-0020: catálogo de número de trabajadores (catalog-database-proposal, "Catálogos sin
-- relaciones de selección entre sí"). Valores de specs/domain/catalog-values.md (catálogo 10) con sus IDs
-- originales, tal como vienen del catálogo de origen.

create table worker_count_range (
    id          integer      not null primary key,
    description varchar(500) not null
);

insert into worker_count_range (id, description) values
    (1, '0'),
    (2, '1-10'),
    (3, '11-30'),
    (4, '31-50'),
    (5, '51-100'),
    (6, '101-251'),
    (7, 'MÁS DE 251');
