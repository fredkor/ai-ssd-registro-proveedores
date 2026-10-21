-- ADR-0017/ADR-0018: tablas de aceptación del aviso, evidencia de firma y contexto JWT.
-- Tipos y constraints según specs/architecture/registration-database-design.md (§5, §7.1).

create table notice_acceptance (
    id          uuid           not null primary key,
    version     varchar(3)     not null,
    accepted_at timestamptz(6) not null,
    constraint notice_acceptance_version_check check (version = '1.0')
);

create table applicant_signature_evidence (
    id                  uuid        not null primary key,
    rfc                 varchar(13) not null,
    verified_legal_name text        not null,
    complete_result     text        not null
);

-- Un registro por token emitido; una aceptación respalda un solo token (ADR-0015).
create table registration_context (
    jti                   uuid           not null primary key,
    rfc                   varchar(13)    not null,
    issued_at             timestamptz(6) not null,
    expires_at            timestamptz(6) not null,
    signature_evidence_id uuid           not null,
    notice_acceptance_id  uuid           not null,
    constraint registration_context_signature_evidence_uk unique (signature_evidence_id),
    constraint registration_context_notice_acceptance_uk unique (notice_acceptance_id),
    constraint registration_context_signature_evidence_fk
        foreign key (signature_evidence_id) references applicant_signature_evidence (id),
    constraint registration_context_notice_acceptance_fk
        foreign key (notice_acceptance_id) references notice_acceptance (id)
);
