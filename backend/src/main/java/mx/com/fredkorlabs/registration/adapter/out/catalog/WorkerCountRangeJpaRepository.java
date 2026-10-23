package mx.com.fredkorlabs.registration.adapter.out.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerCountRangeJpaRepository extends JpaRepository<WorkerCountRangeJpaEntity, Integer> {

    List<WorkerCountRangeJpaEntity> findAllByOrderByIdAsc();
}
