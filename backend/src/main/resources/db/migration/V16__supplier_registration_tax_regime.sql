-- Régimen del expediente para reportes (registration-database-design §1; INV-14). Es una copia del
-- régimen que se deriva del RFC: 12 caracteres PERSONA MORAL (1), 13 PERSONA FÍSICA (2) (catálogo 1).

alter table supplier_registration add column tax_regime_id integer;

-- Expedientes existentes: se deriva de su RFC antes de volver obligatoria la columna.
update supplier_registration set tax_regime_id = case length(rfc) when 12 then 1 else 2 end;

alter table supplier_registration alter column tax_regime_id set not null;

alter table supplier_registration add constraint supplier_registration_tax_regime_fk
    foreign key (tax_regime_id) references tax_regime (id);

-- Nunca contradice INV-14.
alter table supplier_registration add constraint supplier_registration_tax_regime_check
    check ((length(rfc) = 12 and tax_regime_id = 1) or (length(rfc) = 13 and tax_regime_id = 2));
