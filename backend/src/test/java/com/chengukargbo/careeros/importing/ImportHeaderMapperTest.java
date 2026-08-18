package com.chengukargbo.careeros.importing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ImportHeaderMapperTest {

    private final ImportHeaderMapper mapper = new ImportHeaderMapper();

    @Test
    void mapsCanonicalHeadersAndExplicitAliasesIgnoringCaseAndSpacing() {
        HeaderMappingResult result = mapper.map(Map.of(
            " position_title ", "Engineer",
            "Company Name", "Acme",
            "Remote Type", "Remote",
            "Apply URL", "https://example.test/job",
            "Fit", "8.5",
            "Job Notes", "job",
            "Application Notes", "application"
        ));

        assertThat(result.errors()).isEmpty();
        assertThat(result.fields()).containsEntry("position_title", "Engineer")
            .containsEntry("company_name", "Acme")
            .containsEntry("work_arrangement", "Remote")
            .containsEntry("application_url", "https://example.test/job")
            .containsEntry("match_score", "8.5")
            .containsEntry("job_notes", "job")
            .containsEntry("application_notes", "application");
    }

    @Test
    void warnsForUnknownHeaders() {
        HeaderMappingResult result = mapper.map(Map.of("Mystery", "value"));

        assertThat(result.fields()).isEmpty();
        assertThat(result.warnings()).singleElement()
            .extracting(ImportIssue::field)
            .isEqualTo("Mystery");
    }

    @Test
    void silentlyIgnoresRecognizedExportOnlyHeaders() {
        HeaderMappingResult result = mapper.map(Map.ofEntries(
            Map.entry("job_id", "1"),
            Map.entry("company_id", "2"),
            Map.entry("job_created_at", "2026-08-01T10:00:00Z"),
            Map.entry("job_updated_at", "2026-08-02T10:00:00Z"),
            Map.entry("application_id", "3"),
            Map.entry("application_created_at", "2026-08-03T10:00:00Z"),
            Map.entry("application_updated_at", "2026-08-04T10:00:00Z")
        ));

        assertThat(result.fields()).isEmpty();
        assertThat(result.errors()).isEmpty();
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void ignoresOnlyKnownSystemHeadersWhileUnknownHeadersStillWarn() {
        HeaderMappingResult result = mapper.map(Map.of(
            "Job ID", "1",
            "Future Internal Value", "value"
        ));

        assertThat(result.fields()).isEmpty();
        assertThat(result.warnings()).singleElement()
            .extracting(ImportIssue::field)
            .isEqualTo("Future Internal Value");
    }

    @Test
    void rejectsDuplicateMappedHeaders() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Company", "Acme");
        fields.put("Company Name", "Other");

        HeaderMappingResult result = mapper.map(fields);

        assertThat(result.errors()).singleElement()
            .extracting(ImportIssue::field)
            .isEqualTo("company_name");
    }
}
