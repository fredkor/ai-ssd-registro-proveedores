-- ADR-0017/ADR-0020: catálogo de ventas anuales (catalog-database-proposal, "Catálogos sin relaciones de
-- selección entre sí"). Valores de specs/domain/catalog-values.md (catálogo 11) con sus IDs originales.

create table annual_sales_range (
    id          integer      not null primary key,
    description varchar(500) not null
);

insert into annual_sales_range (id, description) values
    (1, '$0 - $4 MILLONES'),
    (2, '$4 - $100 MILLONES'),
    (3, '$100 - $250 MILLONES'),
    (4, 'MÁS DE $250 MILLONES');
