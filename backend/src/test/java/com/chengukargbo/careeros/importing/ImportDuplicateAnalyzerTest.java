package com.chengukargbo.careeros.importing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.companies.Company;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;
import com.chengukargbo.careeros.jobs.RemoteType;

class ImportDuplicateAnalyzerTest {

    private JobOpportunityRepository repository;
    private ImportRowNormalizer normalizer;
    private ImportDuplicateAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        repository = mock(JobOpportunityRepository.class);
        ApplicationUrlNormalizer urlNormalizer = new ApplicationUrlNormalizer();
        normalizer = new ImportRowNormalizer(
            new ImportHeaderMapper(), urlNormalizer
        );
        analyzer = new ImportDuplicateAnalyzer(repository, urlNormalizer);
    }

    @Test
    void blocksPersistedNormalizedUrlDuplicatesButAllowsNonduplicates() {
        when(repository.findAll()).thenReturn(List.of(job(
            company("Acme"), "Engineer", "HTTPS://EXAMPLE.COM:443/jobs/1#apply"
        )));

        List<ImportRowResult> results = analyzer.analyze(List.of(
            row(2, "Acme", "Different", "https://example.com/jobs/1"),
            row(3, "Acme", "Other", "https://example.com/jobs/2")
        ));

        assertThat(results.get(0).exactUrlDuplicate()).isNotNull();
        assertThat(results.get(0).proposedAction())
            .isEqualTo(ImportProposedAction.SKIP_DUPLICATE);
        assertThat(results.get(0).selectable()).isFalse();
        assertThat(results.get(1).proposedAction())
            .isEqualTo(ImportProposedAction.CREATE);
    }

    @Test
    void companyAndTitleMatchesWarnWithoutBlocking() {
        when(repository.findAll()).thenReturn(List.of(
            job(company(" Acme "), "Staff Engineer", null),
            job(company("Other"), "Designer", null)
        ));

        List<ImportRowResult> results = analyzer.analyze(List.of(
            row(2, "acme", " staff engineer ", null),
            row(3, "Acme", "Different", null),
            row(4, "Different", "Staff Engineer", null)
        ));

        assertThat(results.get(0).companyTitleDuplicateCandidates()).hasSize(1);
        assertThat(results.get(0).proposedAction())
            .isEqualTo(ImportProposedAction.REVIEW_WARNING);
        assertThat(results.get(0).selectable()).isTrue();
        assertThat(results.get(1).companyTitleDuplicateCandidates()).isEmpty();
        assertThat(results.get(2).companyTitleDuplicateCandidates()).isEmpty();
    }

    @Test
    void detectsInBatchUrlAndCompanyTitleDuplicatesAgainstEarlierRows() {
        when(repository.findAll()).thenReturn(List.of());

        List<ImportRowResult> results = analyzer.analyze(List.of(
            row(2, "Acme", "Engineer", "https://example.com/job#one"),
            row(3, "ACME", "ENGINEER", "https://EXAMPLE.com/job#two")
        ));

        assertThat(results.get(0).proposedAction())
            .isEqualTo(ImportProposedAction.CREATE);
        assertThat(results.get(1).exactUrlDuplicate().importRowNumber())
            .isEqualTo(2);
        assertThat(results.get(1).companyTitleDuplicateCandidates())
            .extracting(ImportDuplicateMatch::importRowNumber)
            .containsExactly(2);
        assertThat(results.get(1).proposedAction())
            .isEqualTo(ImportProposedAction.SKIP_DUPLICATE);
    }

    @Test
    void invalidRowsRemainUnselectableEvenWhenWarningsExist() {
        when(repository.findAll()).thenReturn(List.of(
            job(company("Acme"), "Engineer", null)
        ));
        ImportRowResult invalid = normalizer.normalize(2, Map.of(
            "company_name", "Acme"
        ));

        ImportRowResult result = analyzer.analyze(List.of(invalid)).getFirst();

        assertThat(result.proposedAction()).isEqualTo(ImportProposedAction.INVALID);
        assertThat(result.selectable()).isFalse();
    }

    private ImportRowResult row(
        int rowNumber,
        String company,
        String title,
        String url
    ) {
        java.util.HashMap<String, String> fields = new java.util.HashMap<>();
        fields.put("company_name", company);
        fields.put("position_title", title);
        if (url != null) {
            fields.put("application_url", url);
        }
        return normalizer.normalize(rowNumber, fields);
    }

    private Company company(String name) {
        return new Company(
            name, null, null, null, null, null, null, null, null,
            null, null, false
        );
    }

    private JobOpportunity job(Company company, String title, String url) {
        return new JobOpportunity(
            company, title, null, null, RemoteType.UNKNOWN, null,
            null, null, "USD", null, url, null, null, null,
            (short) 3, null, null, null
        );
    }
}
