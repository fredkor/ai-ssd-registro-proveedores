package mx.com.fredkorlabs.registration.adapter.out.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubsectorJpaRepository extends JpaRepository<SubsectorJpaEntity, Integer> {

    List<SubsectorJpaEntity> findBySectorIdOrderByIdAsc(Integer sectorId);
}
