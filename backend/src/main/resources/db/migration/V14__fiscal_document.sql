-- ADR-0010/ADR-0017: metadatos de archivos y asociación de la cédula fiscal (registration-database-design §4,
-- §7.1). Los bytes viven en el volumen; aquí solo metadatos, ruta relativa y referencia activa.

create table file_metadata (
    id                    uuid           not null primary key,
    owner_registration_id uuid           not null,
    original_name         text           not null,
    relative_path         varchar(255)   not null,
    extension             varchar(10)    not null,
    media_type            text           not null,
    size_bytes            bigint         not null,
    uploaded_at           timestamptz(6) not null,
    associated            boolean        not null,
    constraint file_metadata_relative_path_uk unique (relative_path),
    -- Target of the fiscal document reference, so the file owner must match the registration.
    constraint file_metadata_id_owner_uk unique (id, owner_registration_id),
    constraint file_metadata_owner_fk
        foreign key (owner_registration_id) references supplier_registration (id)
);

-- Limpieza diaria: no asociados por antigüedad (ADR-0010).
create index file_metadata_associated_uploaded_at_idx on file_metadata (associated, uploaded_at);

-- Una cédula por expediente y un archivo por cédula; el dueño del archivo es el mismo expediente.
create table registration_fiscal_document (
    registration_id uuid not null primary key,
    file_id         uuid not null,
    constraint registration_fiscal_document_file_uk unique (file_id),
    constraint registration_fiscal_document_registration_fk
        foreign key (registration_id) references supplier_registration (id),
    constraint registration_fiscal_document_file_owner_fk
        foreign key (file_id, registration_id) references file_metadata (id, owner_registration_id)
);
