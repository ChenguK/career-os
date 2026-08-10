package com.chengukargbo.careeros.importing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ImportBatchRowRepository
    extends JpaRepository<ImportBatchRow, Long> {
    Page<ImportBatchRow> findByImportBatchIdOrderBySourceRowNumberAscIdAsc(
        Long importBatchId,
        Pageable pageable
    );
}
