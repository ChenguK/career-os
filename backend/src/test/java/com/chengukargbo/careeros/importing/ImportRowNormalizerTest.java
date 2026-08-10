package com.chengukargbo.careeros.importing;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.jobs.RemoteType;

class ImportRowNormalizerTest {

    private final ImportRowNormalizer normalizer = new ImportRowNormalizer(
        new ImportHeaderMapper(), new ApplicationUrlNormalizer()
    );

    @Test
    void normalizesStringsDefaultsEnumsBooleansNumbersAndDates() {
        ImportRowResult result = normalize(Map.ofEntries(
            Map.entry("Job Title", "  Staff   Engineer  "),
            Map.entry("Company", " Acme "),
            Map.entry("department", "   "),
            Map.entry("Work Type", "on-site"),
            Map.entry("salary_min", "$120,000"),
            Map.entry("salary_max", "150000.50"),
            Map.entry("priority", "5"),
            Map.entry("Fit", "9.5"),
            Map.entry("status", "Interview 1"),
            Map.entry("cover_letter_needed", "YES"),
            Map.entry("date_posted", "2026-08-09"),
            Map.entry("closing_date", "08/31/2026"),
            Map.entry("phone_screen_at", "2026-08-09T14:30:00-04:00")
        ));

        CanonicalImportRow row = result.values();
        assertThat(result.errors()).isEmpty();
        assertThat(row.positionTitle()).isEqualTo("Staff   Engineer");
        assertThat(row.companyName()).isEqualTo("Acme");
        assertThat(row.department()).isNull();
        assertThat(row.workArrangement()).isEqualTo(RemoteType.ONSITE);
        assertThat(row.salaryMin()).isEqualByComparingTo("120000");
        assertThat(row.salaryMax()).isEqualByComparingTo("150000.50");
        assertThat(row.salaryCurrency()).isEqualTo("USD");
        assertThat(row.priority()).isEqualTo((short) 5);
        assertThat(row.matchScore()).isEqualByComparingTo("9.5");
        assertThat(row.status()).isEqualTo(ApplicationStatus.INTERVIEW_ONE);
        assertThat(row.coverLetterNeeded()).isTrue();
        assertThat(row.datePosted()).isEqualTo(LocalDate.of(2026, 8, 9));
        assertThat(row.closingDate()).isEqualTo(LocalDate.of(2026, 8, 31));
        assertThat(row.phoneScreenAt()).isEqualTo(
            OffsetDateTime.parse("2026-08-09T14:30:00-04:00")
        );
    }

    @Test
    void acceptsCanonicalAndDisplayEnumAndBooleanVariants() {
        for (String value : new String[]{"Remote", " remote ", "REMOTE"}) {
            assertThat(normalize(Map.of(
                "position_title", "Engineer", "work_arrangement", value
            )).values().workArrangement()).isEqualTo(RemoteType.REMOTE);
        }
        assertThat(normalize(Map.of(
            "position_title", "Engineer", "status", "Phone Screen"
        )).values().status()).isEqualTo(ApplicationStatus.PHONE_SCREEN);
        for (String value : new String[]{"true", "yes", "y", "1"}) {
            assertThat(normalize(Map.of(
                "position_title", "Engineer", "cover_letter_needed", value
            )).values().coverLetterNeeded()).isTrue();
        }
        for (String value : new String[]{"false", "no", "n", "0", " "}) {
            assertThat(normalize(Map.of(
                "position_title", "Engineer", "cover_letter_needed", value
            )).values().coverLetterNeeded()).isFalse();
        }
    }

    @Test
    void reportsInvalidEnumsBooleansNumbersRangesAndLengths() {
        ImportRowResult result = normalize(Map.ofEntries(
            Map.entry("position_title", "x".repeat(201)),
            Map.entry("work_arrangement", "sometimes remote"),
            Map.entry("status", "maybe applied"),
            Map.entry("cover_letter_needed", "perhaps"),
            Map.entry("salary_min", "-1"),
            Map.entry("salary_max", "not money"),
            Map.entry("priority", "6"),
            Map.entry("match_score", "10.1"),
            Map.entry("recruiter_email", "not-an-email")
        ));

        assertThat(result.selectable()).isFalse();
        assertThat(result.proposedAction()).isEqualTo(ImportProposedAction.INVALID);
        assertThat(result.errors()).extracting(ImportIssue::field)
            .contains("position_title", "work_arrangement", "status",
                "cover_letter_needed", "salary_min", "salary_max",
                "priority", "match_score", "recruiter_email");
    }

    @Test
    void rejectsReversedSalaryRangeAndMissingRequiredTitle() {
        ImportRowResult result = normalize(Map.of(
            "salary_min", "120000", "salary_max", "100000"
        ));
        assertThat(result.errors()).extracting(ImportIssue::field)
            .contains("salary_max", "position_title");
    }

    @Test
    void acceptsOnlyExplicitDatesAndOffsetDateTimes() {
        assertThat(normalize(Map.of(
            "position_title", "Engineer", "application_date", "01/02/03",
            "phone_screen_at", "2026-08-09T14:30:00"
        )).errors()).extracting(ImportIssue::field)
            .containsExactlyInAnyOrder("application_date", "phone_screen_at");
        assertThat(normalize(Map.of(
            "position_title", "Engineer", "follow_up_date", "bad",
            "offer_at", "not-a-date"
        )).errors()).extracting(ImportIssue::field)
            .containsExactlyInAnyOrder("follow_up_date", "offer_at");
    }

    @Test
    void validRowsRemainSelectableAndKeepJobAndApplicationNotesSeparate() {
        ImportRowResult result = normalize(Map.of(
            "position_title", "Engineer",
            "Job Notes", "job note",
            "Application Notes", "application note"
        ));
        assertThat(result.selectable()).isTrue();
        assertThat(result.values().jobNotes()).isEqualTo("job note");
        assertThat(result.values().applicationNotes()).isEqualTo("application note");
    }

    private ImportRowResult normalize(Map<String, String> fields) {
        return normalizer.normalize(7, fields);
    }
}
