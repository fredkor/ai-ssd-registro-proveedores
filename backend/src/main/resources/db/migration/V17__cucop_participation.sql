-- Participación CUCoP del expediente (registration-database-design §3). La fila de cucop_participation
-- aparece al primer guardado válido; tipos y específicas se reemplazan completos en cada guardado.

create table cucop_participation (
    registration_id uuid not null primary key,
    constraint cucop_participation_registration_fk
        foreign key (registration_id) references supplier_registration (id)
);

create table registration_participation_type (
    registration_id    uuid        not null,
    participation_type varchar(32) not null,
    constraint registration_participation_type_pk primary key (registration_id, participation_type),
    constraint registration_participation_type_check
        check (participation_type in ('GOODS_AND_SERVICES_SUPPLIER', 'PUBLIC_WORKS_CONTRACTOR')),
    constraint registration_participation_type_participation_fk
        foreign key (registration_id) references cucop_participation (registration_id)
);

-- Solo partidas específicas (nivel 4); capítulos, conceptos y genéricas no se guardan como selección.
create table registration_cucop_item (
    registration_id  uuid    not null,
    specific_item_id integer not null,
    constraint registration_cucop_item_pk primary key (registration_id, specific_item_id),
    constraint registration_cucop_item_participation_fk
        foreign key (registration_id) references cucop_participation (registration_id),
    constraint registration_cucop_item_specific_item_fk
        foreign key (specific_item_id) references cucop_specific_item (id)
);

create index registration_cucop_item_specific_item_id_idx on registration_cucop_item (specific_item_id);
