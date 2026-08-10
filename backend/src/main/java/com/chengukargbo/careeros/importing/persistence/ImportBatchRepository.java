package com.chengukargbo.careeros.importing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    @Query("""
        SELECT batch FROM ImportBatch batch
        ORDER BY COALESCE(batch.completedAt, batch.createdAt) DESC,
                 batch.id DESC
        """)
    Page<ImportBatch> findHistory(Pageable pageable);
}
