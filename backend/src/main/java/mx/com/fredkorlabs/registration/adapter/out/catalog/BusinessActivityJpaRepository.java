package mx.com.fredkorlabs.registration.adapter.out.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessActivityJpaRepository extends JpaRepository<BusinessActivityJpaEntity, Integer> {

    List<BusinessActivityJpaEntity> findAllByOrderByIdAsc();
}
