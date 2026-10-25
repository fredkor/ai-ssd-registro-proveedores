package mx.com.fredkorlabs.registration.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FileMetadataJpaRepository extends JpaRepository<FileMetadataJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<FileMetadataJpaEntity> findByAssociatedFalseAndUploadedAtBeforeOrderByUploadedAtAsc(Instant instant);
}
