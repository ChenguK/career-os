package com.chengukargbo.careeros.importing.persistence;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportBatchMetadataService {
    private final ImportBatchRepository batchRepository;
    private final ImportBatchRowRepository rowRepository;

    public ImportBatchMetadataService(
        ImportBatchRepository batchRepository,
        ImportBatchRowRepository rowRepository
    ) {
        this.batchRepository = batchRepository;
        this.rowRepository = rowRepository;
    }

    @Transactional
    public ImportBatch create(String filename, int total, int selected) {
        return batchRepository.saveAndFlush(
            new ImportBatch(filename, total, selected)
        );
    }

    @Transactional
    public ImportBatch complete(
        ImportBatch batch,
        List<ImportRowPersistenceResult> results
    ) {
        int created = count(results, ImportRowOutcomeStatus.CREATED);
        int warnings = count(
            results, ImportRowOutcomeStatus.CREATED_WITH_WARNING
        );
        int skipped = count(
            results, ImportRowOutcomeStatus.SKIPPED_DUPLICATE
        );
        int failed = results.size() - created - warnings - skipped;
        batch.complete(created, warnings, skipped, failed);
        rowRepository.saveAll(results.stream()
            .map(result -> new ImportBatchRow(batch.getId(), result))
            .toList());
        return batchRepository.saveAndFlush(batch);
    }

    private int count(
        List<ImportRowPersistenceResult> results,
        ImportRowOutcomeStatus status
    ) {
        return (int) results.stream()
            .filter(result -> result.status() == status)
            .count();
    }
}
