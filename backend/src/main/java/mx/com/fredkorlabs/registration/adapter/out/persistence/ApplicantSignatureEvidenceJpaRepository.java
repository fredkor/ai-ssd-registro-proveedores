package mx.com.fredkorlabs.registration.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApplicantSignatureEvidenceJpaRepository extends JpaRepository<ApplicantSignatureEvidenceJpaEntity, UUID> {
}
