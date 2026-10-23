package mx.com.fredkorlabs.registration.adapter.out.catalog;

import mx.com.fredkorlabs.registration.domain.model.LegalOwnershipOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LegalOwnershipJpaRepository extends JpaRepository<LegalOwnershipJpaEntity, Integer> {

    @Query("select o from LegalOwnershipJpaEntity o where o.isoCountry = :countryCode or o.key = '"
        + LegalOwnershipOption.NOT_DEFINED_KEY + "'")
    List<LegalOwnershipJpaEntity> findByCountryOrNotDefined(@Param("countryCode") String countryCode);
}
