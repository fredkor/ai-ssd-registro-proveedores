package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "applicant_signature_evidence")
public class ApplicantSignatureEvidenceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "rfc", nullable = false, updatable = false, length = 13)
    private String rfc;

    @Column(name = "verified_legal_name", nullable = false, updatable = false, columnDefinition = "varchar")
    private String verifiedLegalName;

    // Original ResultadoFirma JSON, exactly as received (registration-database-design §5).
    @Lob
    @Column(name = "complete_result", nullable = false, updatable = false)
    private String completeResult;

    protected ApplicantSignatureEvidenceJpaEntity() {
    }

    public ApplicantSignatureEvidenceJpaEntity(String rfc, String verifiedLegalName, String completeResult) {
        this.rfc = rfc;
        this.verifiedLegalName = verifiedLegalName;
        this.completeResult = completeResult;
    }

    public UUID getId() { return id; }

    public String getRfc() { return rfc; }

    public String getVerifiedLegalName() { return verifiedLegalName; }

    public String getCompleteResult() { return completeResult; }
}
