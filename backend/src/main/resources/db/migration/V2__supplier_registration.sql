-- ADR-0017/ADR-0018: expediente del registro (registration-database-design §1, §7.1).
-- Datos Generales, progreso y vínculos de evidencia se crean con las operaciones que los usan.

create table supplier_registration (
    id                   uuid        not null primary key,
    rfc                  varchar(13) not null,
    legal_name           text        not null,
    status               varchar(32) not null,
    notice_acceptance_id uuid        not null,
    initial_evidence_id  uuid        not null,
    constraint supplier_registration_rfc_uk unique (rfc),
    constraint supplier_registration_notice_acceptance_uk unique (notice_acceptance_id),
    constraint supplier_registration_initial_evidence_uk unique (initial_evidence_id),
    constraint supplier_registration_status_check check (status in ('IN_PROGRESS', 'REGISTERED')),
    constraint supplier_registration_notice_acceptance_fk
        foreign key (notice_acceptance_id) references notice_acceptance (id),
    constraint supplier_registration_initial_evidence_fk
        foreign key (initial_evidence_id) references applicant_signature_evidence (id)
);
