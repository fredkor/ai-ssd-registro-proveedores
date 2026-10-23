-- ADR-0017: catálogo de origen y Datos Generales del expediente con sus teléfonos
-- (registration-database-design §2, §7.1; catalog-database-proposal). Valores de origen de
-- specs/domain/catalog-values.md (catálogo 2): NATIONAL es el ID 1 y FOREIGN el ID 2 (domain/catalogs.md).

create table origin (
    id          integer      not null primary key,
    description varchar(500) not null
);

insert into origin (id, description) values
    (1, 'NACIONAL'),
    (2, 'EXTRANJERO');

-- La fila aparece con el primer guardado válido; las selecciones referencian el ID de cada catálogo.
create table general_data (
    registration_id       uuid          not null primary key,
    origin_id             integer       not null,
    country_id            integer       not null,
    nationality           varchar(250)  not null,
    foreign_fiscal_code   varchar(20),
    legal_ownership_id    integer       not null,
    sector_id             integer,
    subsector_id          integer,
    business_activity_id  integer       not null,
    stratification_id     integer       not null,
    primary_activity_id   integer       not null,
    worker_count_range_id integer       not null,
    annual_sales_range_id integer       not null,
    website               varchar(2048),
    constraint general_data_registration_fk foreign key (registration_id) references supplier_registration (id),
    constraint general_data_origin_fk foreign key (origin_id) references origin (id),
    constraint general_data_country_fk foreign key (country_id) references country (id),
    constraint general_data_legal_ownership_fk foreign key (legal_ownership_id) references legal_ownership (id),
    constraint general_data_sector_fk foreign key (sector_id) references sector (id),
    constraint general_data_subsector_fk foreign key (subsector_id) references subsector (id),
    constraint general_data_business_activity_fk foreign key (business_activity_id) references business_activity (id),
    constraint general_data_stratification_fk foreign key (stratification_id) references stratification (id),
    constraint general_data_primary_activity_fk foreign key (primary_activity_id) references primary_activity (id),
    constraint general_data_worker_count_range_fk
        foreign key (worker_count_range_id) references worker_count_range (id),
    constraint general_data_annual_sales_range_fk
        foreign key (annual_sales_range_id) references annual_sales_range (id)
);

-- Persona moral: colección completa reemplazada en cada guardado; persona física no tiene filas.
create table registration_phone (
    registration_id uuid        not null,
    position        integer     not null,
    number          varchar(10) not null,
    extension       varchar(5),
    constraint registration_phone_pk primary key (registration_id, position),
    constraint registration_phone_general_data_fk
        foreign key (registration_id) references general_data (registration_id)
);
