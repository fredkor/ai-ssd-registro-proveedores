-- ADR-0017: catálogos de estatus y de tipo de proveedor (catalog-database-proposal, "Catálogos sin
-- relaciones de selección entre sí"). Son catálogos distintos y ninguno representa RegistrationStatus.
-- Valores de specs/domain/catalog-values.md (catálogos 3 y 13) con sus IDs originales. No se capturan
-- en el formulario de la primera etapa.

create table supplier_status (
    id          integer      not null primary key,
    description varchar(500) not null
);

insert into supplier_status (id, description) values
    (1, 'POTENCIAL LICITANTE'),
    (2, 'LICITANTE'),
    (3, 'PROVEEDOR Y/O CONTRATISTA');

create table supplier_type (
    id          integer      not null primary key,
    description varchar(500) not null
);

insert into supplier_type (id, description) values
    (1, 'RECURSOS NATURALES'),
    (2, 'CONSTRUCCIÓN'),
    (3, 'FABRICACIÓN, PRODUCCIÓN O ELABORACIÓN'),
    (4, 'COMERCIO AL POR MAYOR'),
    (5, 'TRANSPORTE'),
    (6, 'COMUNICACIÓN'),
    (7, 'SERVICIOS'),
    (8, 'COMERCIO AL POR MENOR');
