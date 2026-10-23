-- ADR-0017/ADR-0018: progreso por sección e historial de evidencias de firma del expediente
-- (registration-database-design §1, §5, §7.1). El alta crea las seis secciones PENDING.

create table section_progress (
    registration_id uuid        not null,
    section         varchar(32) not null,
    status          varchar(32) not null,
    constraint section_progress_pk primary key (registration_id, section),
    constraint section_progress_section_check check (section in
        ('GENERAL_DATA', 'CUCOP', 'CONTACTS', 'FISCAL_ADDRESS', 'COVERAGE', 'ELECTRONIC_SIGNATURE')),
    constraint section_progress_status_check check (status in ('PENDING', 'COMPLETED', 'REQUIRES_REVIEW')),
    constraint section_progress_registration_fk
        foreign key (registration_id) references supplier_registration (id)
);

-- Una evidencia se vincula a un solo expediente; incluye la evidencia inicial.
create table registration_signature_evidence (
    registration_id       uuid not null,
    signature_evidence_id uuid not null,
    constraint registration_signature_evidence_pk primary key (registration_id, signature_evidence_id),
    constraint registration_signature_evidence_evidence_uk unique (signature_evidence_id),
    constraint registration_signature_evidence_registration_fk
        foreign key (registration_id) references supplier_registration (id),
    constraint registration_signature_evidence_evidence_fk
        foreign key (signature_evidence_id) references applicant_signature_evidence (id)
);
