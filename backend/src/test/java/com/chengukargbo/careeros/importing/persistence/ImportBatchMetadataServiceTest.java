package com.chengukargbo.careeros.importing.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ImportBatchMetadataServiceTest {

    @Test
    void recordsXlsxTransportFormatFromFilename() {
        ImportBatchRepository batches = mock(ImportBatchRepository.class);
        ImportBatchRowRepository rows = mock(ImportBatchRowRepository.class);
        when(batches.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ImportBatch batch = new ImportBatchMetadataService(batches, rows)
            .create("career-tracker.XLSX", 2, 1);

        assertThat(batch.getFormat()).isEqualTo("XLSX");
    }

    @Test
    void recordsSchemaFormatCountsCompletionAndRowOutcomes() {
        ImportBatchRepository batches = mock(ImportBatchRepository.class);
        ImportBatchRowRepository rows = mock(ImportBatchRowRepository.class);
        when(batches.saveAndFlush(any())).thenAnswer(invocation -> {
            ImportBatch batch = invocation.getArgument(0);
            if (batch.getId() == null) {
                ReflectionTestUtils.setField(batch, "id", 1L);
                ReflectionTestUtils.setField(
                    batch, "createdAt", java.time.OffsetDateTime.now()
                );
            }
            return batch;
        });
        ImportBatchMetadataService service =
            new ImportBatchMetadataService(batches, rows);

        ImportBatch batch = service.create("jobs.csv", 8, 4);
        ImportBatch completed = service.complete(batch, List.of(
            result(2, ImportRowOutcomeStatus.CREATED),
            result(3, ImportRowOutcomeStatus.CREATED_WITH_WARNING),
            result(4, ImportRowOutcomeStatus.SKIPPED_DUPLICATE),
            result(5, ImportRowOutcomeStatus.FAILED_VALIDATION)
        ));

        assertThat(completed.getOriginalFilename()).isEqualTo("jobs.csv");
        assertThat(completed.getFormat()).isEqualTo("CSV");
        assertThat(completed.getSchemaVersion())
            .isEqualTo("careeros_job_import_v1");
        assertThat(completed.getTotalRowCount()).isEqualTo(8);
        assertThat(completed.getSelectedRowCount()).isEqualTo(4);
        assertThat(completed.getCreatedRowCount()).isEqualTo(1);
        assertThat(completed.getWarningCreatedRowCount()).isEqualTo(1);
        assertThat(completed.getSkippedDuplicateRowCount()).isEqualTo(1);
        assertThat(completed.getFailedRowCount()).isEqualTo(1);
        assertThat(completed.getCompletedAt()).isNotNull();
        verify(rows).saveAll(any());
    }

    private ImportRowPersistenceResult result(
        int row,
        ImportRowOutcomeStatus status
    ) {
        return new ImportRowPersistenceResult(
            row, status, null, null, null, null, List.of(), List.of()
        );
    }
}
