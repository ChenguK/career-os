package com.chengukargbo.careeros.importing.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.chengukargbo.careeros.importing.ApplicationUrlNormalizer;
import com.chengukargbo.careeros.importing.ImportAnalysisService;
import com.chengukargbo.careeros.importing.ImportDuplicateAnalyzer;
import com.chengukargbo.careeros.importing.ImportHeaderMapper;
import com.chengukargbo.careeros.importing.ImportRowNormalizer;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;

class ImportPersistenceServiceTest {

    private ImportRowPersistenceService rowService;
    private ImportBatchMetadataService metadata;
    private ImportPersistenceService service;

    @BeforeEach
    void setUp() {
        JobOpportunityRepository jobs = mock(JobOpportunityRepository.class);
        when(jobs.findAll()).thenReturn(List.of());
        ApplicationUrlNormalizer urls = new ApplicationUrlNormalizer();
        ImportAnalysisService analysis = new ImportAnalysisService(
            new ImportRowNormalizer(new ImportHeaderMapper(), urls),
            new ImportDuplicateAnalyzer(jobs, urls)
        );
        rowService = mock(ImportRowPersistenceService.class);
        metadata = mock(ImportBatchMetadataService.class);
        service = new ImportPersistenceService(analysis, rowService, metadata);

        when(metadata.create(anyString(), anyInt(), anyInt()))
            .thenAnswer(invocation -> {
                ImportBatch batch = new ImportBatch(
                    invocation.getArgument(0),
                    invocation.getArgument(1),
                    invocation.getArgument(2)
                );
                ReflectionTestUtils.setField(batch, "id", 77L);
                return batch;
            });
        when(metadata.complete(any(), anyList())).thenAnswer(invocation -> {
            ImportBatch batch = invocation.getArgument(0);
            List<ImportRowPersistenceResult> results = invocation.getArgument(1);
            int created = count(results, ImportRowOutcomeStatus.CREATED);
            int warnings = count(results, ImportRowOutcomeStatus.CREATED_WITH_WARNING);
            int skipped = count(results, ImportRowOutcomeStatus.SKIPPED_DUPLICATE);
            batch.complete(created, warnings, skipped,
                results.size() - created - warnings - skipped);
            return batch;
        });
    }

    @Test
    void revalidatesTamperedInvalidRowsAndNeverCallsPersistence() {
        ImportPersistenceResponse response = service.persist(request(
            selected(2, Map.of(
                "position_title", "Engineer", "priority", "99"
            ))
        ));

        assertThat(response.failed()).isEqualTo(1);
        assertThat(response.rows().getFirst().status())
            .isEqualTo(ImportRowOutcomeStatus.FAILED_VALIDATION);
        assertThat(response.rows().getFirst().errors())
            .contains("Priority must be between 1 and 5");
        verify(rowService, times(0)).persist(any());
    }

    @Test
    void onlyOneInRequestNormalizedUrlDuplicateCanPersist() {
        when(rowService.persist(any())).thenReturn(created(2));
        ImportPersistenceResponse response = service.persist(request(
            selected(2, Map.of(
                "position_title", "One",
                "application_url", "https://example.com/job#one"
            )),
            selected(3, Map.of(
                "position_title", "Two",
                "application_url", "HTTPS://EXAMPLE.COM:443/job#two"
            ))
        ));

        assertThat(response.created()).isEqualTo(1);
        assertThat(response.skippedDuplicates()).isEqualTo(1);
        verify(rowService).persist(any());
    }

    @Test
    void onePersistenceFailureDoesNotHideASuccessfulRow() {
        when(rowService.persist(any()))
            .thenThrow(new RuntimeException("application failed"))
            .thenReturn(created(3));

        ImportPersistenceResponse response = service.persist(request(
            selected(2, Map.of("position_title", "One")),
            selected(3, Map.of("position_title", "Two"))
        ));

        assertThat(response.created()).isEqualTo(1);
        assertThat(response.failed()).isEqualTo(1);
        assertThat(response.rows()).extracting(ImportRowPersistenceResult::status)
            .containsExactly(
                ImportRowOutcomeStatus.FAILED_PERSISTENCE,
                ImportRowOutcomeStatus.CREATED
            );
    }

    @Test
    void retriesACompanyRaceOnce() {
        when(rowService.persist(any()))
            .thenThrow(new CompanyResolutionRaceException(
                new RuntimeException("race")
            ))
            .thenReturn(created(2));

        ImportPersistenceResponse response = service.persist(request(
            selected(2, Map.of("position_title", "Engineer"))
        ));

        assertThat(response.created()).isEqualTo(1);
        verify(rowService, times(2)).persist(any());
    }

    @Test
    void returnsBatchIdentityCountsAndCompletionMetadata() {
        when(rowService.persist(any())).thenReturn(new ImportRowPersistenceResult(
            2, ImportRowOutcomeStatus.CREATED_WITH_WARNING,
            10L, 20L, 30L, null, List.of("Possible duplicate"), List.of()
        ));

        ImportPersistenceResponse response = service.persist(request(
            selected(2, Map.of("position_title", "Engineer"))
        ));

        assertThat(response.batchId()).isEqualTo(77L);
        assertThat(response.filename()).isEqualTo("jobs.csv");
        assertThat(response.totalRows()).isEqualTo(5);
        assertThat(response.selectedRows()).isEqualTo(1);
        assertThat(response.createdWithWarnings()).isEqualTo(1);
        assertThat(response.rows().getFirst().jobOpportunityId()).isEqualTo(20L);
        verify(metadata).complete(any(), anyList());
    }

    private ImportPersistenceRequest request(SelectedImportRowRequest... rows) {
        return new ImportPersistenceRequest("jobs.csv", 5, List.of(rows));
    }

    private SelectedImportRowRequest selected(
        int rowNumber,
        Map<String, String> fields
    ) {
        return new SelectedImportRowRequest(rowNumber, fields);
    }

    private ImportRowPersistenceResult created(int rowNumber) {
        return new ImportRowPersistenceResult(
            rowNumber, ImportRowOutcomeStatus.CREATED,
            10L, 20L + rowNumber, 30L + rowNumber, null,
            List.of(), List.of()
        );
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
