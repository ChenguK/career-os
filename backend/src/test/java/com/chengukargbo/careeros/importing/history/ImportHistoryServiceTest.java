package com.chengukargbo.careeros.importing.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.importing.persistence.ImportBatch;
import com.chengukargbo.careeros.importing.persistence.ImportBatchRepository;
import com.chengukargbo.careeros.importing.persistence.ImportBatchRow;
import com.chengukargbo.careeros.importing.persistence.ImportBatchRowRepository;
import com.chengukargbo.careeros.importing.persistence.ImportRowOutcomeStatus;
import com.chengukargbo.careeros.importing.persistence.ImportRowPersistenceResult;

class ImportHistoryServiceTest {
    private ImportBatchRepository batches;
    private ImportBatchRowRepository rows;
    private ImportHistoryService service;

    @BeforeEach
    void setUp() {
        batches = mock(ImportBatchRepository.class);
        rows = mock(ImportBatchRowRepository.class);
        service = new ImportHistoryService(batches, rows);
    }

    @Test
    void returnsBoundedHistoryInRepositoryOrderWithoutBusinessLoading() {
        ImportBatch newest = batch(2L, "new.csv", OffsetDateTime.parse(
            "2026-08-10T12:00:00Z"
        ));
        ImportBatch older = batch(1L, "old.csv", OffsetDateTime.parse(
            "2026-08-09T12:00:00Z"
        ));
        when(batches.findHistory(any())).thenReturn(new PageImpl<>(
            List.of(newest, older), PageRequest.of(0, 25), 2
        ));

        ImportHistoryPage<ImportBatchSummaryResponse> result =
            service.findHistory(0, 25);

        assertThat(result.content()).extracting(
            ImportBatchSummaryResponse::batchId
        ).containsExactly(2L, 1L);
        assertThat(result.totalRows()).isEqualTo(2);
        verify(batches).findHistory(PageRequest.of(0, 25));
        verifyNoMoreInteractions(batches, rows);
    }

    @Test
    void returnsEmptyHistoryAndEnforcesPageBounds() {
        when(batches.findHistory(PageRequest.of(0, 100)))
            .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));
        ImportHistoryPage<ImportBatchSummaryResponse> result =
            service.findHistory(-3, 500);
        assertThat(result.content()).isEmpty();
        assertThat(result.size()).isEqualTo(100);
    }

    @Test
    void returnsOrderedDetailRowsWithEveryOutcomeAndAuditFields() {
        ImportBatch batch = batch(7L, "jobs.csv", OffsetDateTime.now());
        List<ImportBatchRow> outcomes = List.of(
            row(2, ImportRowOutcomeStatus.CREATED, 1L, 2L, 3L, null, "", ""),
            row(3, ImportRowOutcomeStatus.CREATED_WITH_WARNING, 1L, 4L, 5L,
                null, "Possible duplicate", ""),
            row(4, ImportRowOutcomeStatus.SKIPPED_DUPLICATE, null, null, null,
                99L, "URL already exists", ""),
            row(5, ImportRowOutcomeStatus.FAILED_VALIDATION, null, null, null,
                null, "", "Invalid priority"),
            row(6, ImportRowOutcomeStatus.FAILED_PERSISTENCE, null, null, null,
                null, "", "Row could not be persisted")
        );
        when(batches.findById(7L)).thenReturn(Optional.of(batch));
        when(rows.findByImportBatchIdOrderBySourceRowNumberAscIdAsc(
            7L, PageRequest.of(0, 25)
        )).thenReturn(new PageImpl<>(
            outcomes, PageRequest.of(0, 25), outcomes.size()
        ));

        ImportBatchDetailResponse result = service.findBatch(7L, 0, 25);

        assertThat(result.rows().content())
            .extracting(ImportBatchRowResponse::outcome)
            .containsExactly(ImportRowOutcomeStatus.values());
        assertThat(result.rows().content().get(0).companyId()).isEqualTo(1L);
        assertThat(result.rows().content().get(2).duplicateJobOpportunityId())
            .isEqualTo(99L);
        assertThat(result.rows().content().get(1).warnings())
            .containsExactly("Possible duplicate");
        assertThat(result.rows().content().get(3).errors())
            .containsExactly("Invalid priority");
    }

    @Test
    void returnsNotFoundInsteadOfSyntheticBatch() {
        when(batches.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findBatch(404L, 0, 25))
            .isInstanceOf(ImportBatchNotFoundException.class)
            .hasMessage("Import batch not found with id: 404");
        verifyNoMoreInteractions(rows);
    }

    @Test
    void serviceIsExplicitlyReadOnly() {
        Transactional annotation = ImportHistoryService.class
            .getAnnotation(Transactional.class);
        assertThat(annotation.readOnly()).isTrue();
    }

    private ImportBatch batch(Long id, String filename, OffsetDateTime completed) {
        ImportBatch batch = new ImportBatch(filename, 8, 5);
        ReflectionTestUtils.setField(batch, "id", id);
        ReflectionTestUtils.setField(batch, "createdAt", completed.minusMinutes(1));
        batch.complete(1, 1, 1, 2);
        ReflectionTestUtils.setField(batch, "completedAt", completed);
        return batch;
    }

    private ImportBatchRow row(
        int sourceRow,
        ImportRowOutcomeStatus status,
        Long companyId,
        Long jobId,
        Long applicationId,
        Long duplicateId,
        String warnings,
        String errors
    ) {
        ImportBatchRow row = new ImportBatchRow(7L,
            new ImportRowPersistenceResult(
                sourceRow, status, companyId, jobId, applicationId,
                duplicateId,
                warnings.isBlank() ? List.of() : List.of(warnings),
                errors.isBlank() ? List.of() : List.of(errors)
            ));
        ReflectionTestUtils.setField(row, "id", (long) sourceRow);
        return row;
    }
}
