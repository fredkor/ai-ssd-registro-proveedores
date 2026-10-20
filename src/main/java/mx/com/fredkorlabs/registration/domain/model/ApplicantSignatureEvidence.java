package mx.com.fredkorlabs.registration.domain.model;

public class ApplicantSignatureEvidence {

    private final String completeResult;

    public ApplicantSignatureEvidence(String completeResult) {
        if (completeResult == null) throw new IllegalArgumentException("Signature evidence cannot be null");
        this.completeResult = completeResult;
    }

    public String completeResult() {
        return completeResult;
    }
}
