package com.chengukargbo.careeros.importing.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.importing.persistence.ImportBatch;
import com.chengukargbo.careeros.importing.persistence.ImportBatchRepository;
import com.chengukargbo.careeros.importing.persistence.ImportBatchRow;
import com.chengukargbo.careeros.importing.persistence.ImportBatchRowRepository;

@Service
@Transactional(readOnly = true)
public class ImportHistoryService {
    public static final int DEFAULT_SIZE = 25;
    public static final int MAX_SIZE = 100;

    private final ImportBatchRepository batchRepository;
    private final ImportBatchRowRepository rowRepository;

    public ImportHistoryService(
        ImportBatchRepository batchRepository,
        ImportBatchRowRepository rowRepository
    ) {
        this.batchRepository = batchRepository;
        this.rowRepository = rowRepository;
    }

    public ImportHistoryPage<ImportBatchSummaryResponse> findHistory(
        int page,
        int size
    ) {
        Page<ImportBatch> batches = batchRepository.findHistory(
            pageRequest(page, size)
        );
        return new ImportHistoryPage<>(
            batches.getContent().stream()
                .map(ImportBatchSummaryResponse::from)
                .toList(),
            batches.getNumber(), batches.getSize(),
            batches.getTotalElements(), batches.getTotalPages()
        );
    }

    public ImportBatchDetailResponse findBatch(
        Long batchId,
        int page,
        int size
    ) {
        ImportBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new ImportBatchNotFoundException(batchId));
        Page<ImportBatchRow> rows = rowRepository
            .findByImportBatchIdOrderBySourceRowNumberAscIdAsc(
                batchId, pageRequest(page, size)
            );
        return new ImportBatchDetailResponse(
            ImportBatchSummaryResponse.from(batch),
            new ImportHistoryPage<>(
                rows.getContent().stream()
                    .map(ImportBatchRowResponse::from)
                    .toList(),
                rows.getNumber(), rows.getSize(),
                rows.getTotalElements(), rows.getTotalPages()
            )
        );
    }

    private PageRequest pageRequest(int page, int size) {
        int normalizedPage = Math.max(0, page);
        int normalizedSize = size < 1
            ? DEFAULT_SIZE
            : Math.min(size, MAX_SIZE);
        return PageRequest.of(normalizedPage, normalizedSize);
    }
}
