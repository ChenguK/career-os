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
