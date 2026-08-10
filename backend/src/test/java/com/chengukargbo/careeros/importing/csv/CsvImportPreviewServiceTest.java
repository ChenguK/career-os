package com.chengukargbo.careeros.importing.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.ApplicationUrlNormalizer;
import com.chengukargbo.careeros.importing.ImportAnalysisService;
import com.chengukargbo.careeros.importing.ImportHeaderMapper;
import com.chengukargbo.careeros.importing.ImportProposedAction;
import com.chengukargbo.careeros.importing.ImportRowNormalizer;
import com.chengukargbo.careeros.importing.ImportRowResult;

class CsvImportPreviewServiceTest {

    private ImportAnalysisService analysisService;
    private ImportRowNormalizer normalizer;
    private CsvImportPreviewService service;

    @BeforeEach
    void setUp() {
        analysisService = mock(ImportAnalysisService.class);
        ImportHeaderMapper mapper = new ImportHeaderMapper();
        normalizer = new ImportRowNormalizer(
            mapper, new ApplicationUrlNormalizer()
        );
        service = new CsvImportPreviewService(
            new CsvImportParser(mapper), analysisService
        );
    }

    @Test
    void returnsCountsAndRowsFromAuthoritativeAnalysis() {
        List<ImportRowResult> rows = List.of(
            result(2, ImportProposedAction.CREATE, true),
            result(3, ImportProposedAction.REVIEW_WARNING, true),
            result(4, ImportProposedAction.SKIP_DUPLICATE, false),
            result(5, ImportProposedAction.INVALID, false)
        );
        when(analysisService.analyze(anyList())).thenReturn(rows);

        ImportPreviewResponse response = service.preview(file(
            "jobs.csv", "Job Title,Unknown\nEngineer,value\n"
        ));

        assertThat(response.filename()).isEqualTo("jobs.csv");
        assertThat(response.totalRows()).isEqualTo(4);
        assertThat(response.createCount()).isEqualTo(1);
        assertThat(response.reviewCount()).isEqualTo(1);
        assertThat(response.duplicateCount()).isEqualTo(1);
        assertThat(response.invalidCount()).isEqualTo(1);
        assertThat(response.hasFileErrors()).isFalse();
        assertThat(response.hasFileWarnings()).isTrue();
        assertThat(response.fileWarnings()).hasSize(1);
    }

    @Test
    void repeatedPreviewIsSideEffectFreeAndOnlyInvokesAnalysis() {
        when(analysisService.analyze(anyList())).thenReturn(List.of(
            result(2, ImportProposedAction.CREATE, true)
        ));
        MockMultipartFile file = file("jobs.csv", "Job Title\nEngineer\n");

        service.preview(file);
        service.preview(file);

        verify(analysisService, times(2)).analyze(anyList());
    }

    @Test
    void validatesMissingWrongTypeAndOversizedFiles() {
        assertThatThrownBy(() -> service.preview(null))
            .isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> service.preview(file(
            "jobs.xlsx", "Job Title\nEngineer\n"
        ))).isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining(".csv");
        MockMultipartFile wrongMime = new MockMultipartFile(
            "file", "jobs.csv", "application/pdf", "text".getBytes()
        );
        assertThatThrownBy(() -> service.preview(wrongMime))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("CSV text");
    }

    private ImportRowResult result(
        int rowNumber,
        ImportProposedAction action,
        boolean selectable
    ) {
        ImportRowResult base = normalizer.normalize(rowNumber, Map.of(
            "position_title", "Engineer"
        ));
        return new ImportRowResult(
            rowNumber, base.values(), base.errors(), base.warnings(),
            base.normalizedApplicationUrl(), null, List.of(), action, selectable
        );
    }

    private MockMultipartFile file(String name, String content) {
        return new MockMultipartFile(
            "file", name, "text/csv", content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
