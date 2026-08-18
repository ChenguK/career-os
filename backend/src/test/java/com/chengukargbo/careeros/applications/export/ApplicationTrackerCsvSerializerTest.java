package com.chengukargbo.careeros.applications.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.jobs.RemoteType;

class ApplicationTrackerCsvSerializerTest {

    private final ApplicationTrackerCsvSerializer serializer =
        new ApplicationTrackerCsvSerializer();

    @Test
    void writesEveryCanonicalFieldWithEscapingAndMachineReadableValues()
        throws Exception {
        ApplicationTrackerResponse row = fullRow();
        String csv = new String(serializer.serialize(List.of(row)), StandardCharsets.UTF_8);
        List<CSVRecord> records = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .get()
            .parse(new StringReader(csv))
            .getRecords();
        CSVRecord record = records.getFirst();

        assertThat(record.toMap()).hasSize(43);
        assertThat(record.get("job_id")).isEqualTo("7");
        assertThat(record.get("company_name")).isEqualTo("GitHub, Inc.");
        assertThat(record.get("position_title")).isEqualTo("Platform \"Lead\"");
        assertThat(record.get("salary_min")).isEqualTo("-500.25");
        assertThat(record.get("date_posted")).isEqualTo("2026-07-01");
        assertThat(record.get("job_created_at"))
            .isEqualTo("2026-07-01T10:15:30-04:00");
        assertThat(record.get("job_notes")).isEqualTo("job line 1\njob line 2");
        assertThat(record.get("application_notes")).isEqualTo("application notes");
        assertThat(record.get("status")).isEqualTo("APPLIED");
        assertThat(csv).contains("\r\n");
        assertThat(csv).contains("Développeur");
    }

    @Test
    void emitsBlankApplicationCellsAndSavedStatusForJobOnlyRows() throws Exception {
        ApplicationTrackerResponse source = fullRow();
        ApplicationTrackerResponse row = new ApplicationTrackerResponse(
            source.jobOpportunityId(), null, null, source.positionTitle(),
            null, null, RemoteType.UNKNOWN, null, null, null, "USD", null,
            null, null, null, null, (short) 3, null, null, null,
            source.jobCreatedAt(), source.jobUpdatedAt(), null, null, null,
            null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null
        );
        CSVRecord record = parse(serializer.serialize(List.of(row)));

        assertThat(record.get("company_id")).isEmpty();
        assertThat(record.get("application_id")).isEmpty();
        assertThat(record.get("application_date")).isEmpty();
        assertThat(record.get("status")).isEqualTo("SAVED");
    }

    @Test
    void neutralizesFormulaLookingTextWithoutChangingTypedNumbers() throws Exception {
        ApplicationTrackerResponse source = fullRow();
        ApplicationTrackerResponse row = new ApplicationTrackerResponse(
            source.jobOpportunityId(), source.companyId(), "=CMD()", "+title",
            "-department", " @location", source.remoteType(), "@type",
            new BigDecimal("-42.50"), source.salaryMax(), source.salaryCurrency(),
            "=notes", "@url", "+source", source.datePosted(), source.closingDate(),
            (short) 1, new BigDecimal("-9.5"), "-description", "@job notes",
            source.jobCreatedAt(), source.jobUpdatedAt(), source.applicationId(),
            source.status(), "=resume", source.coverLetterNeeded(), source.portfolioLink(),
            source.githubLink(), source.projectsToHighlight(), source.skillsToEmphasize(),
            source.interviewTopics(), source.recruiterName(), source.recruiterEmail(),
            source.applicationDate(), source.followUpDate(), source.phoneScreenAt(),
            source.interviewOneAt(), source.interviewTwoAt(), source.offerAt(),
            source.rejectedAt(), source.applicationNotes(), source.applicationCreatedAt(),
            source.applicationUpdatedAt()
        );
        CSVRecord record = parse(serializer.serialize(List.of(row)));

        assertThat(record.get("company_name")).isEqualTo("'=CMD()");
        assertThat(record.get("position_title")).isEqualTo("'+title");
        assertThat(record.get("department")).isEqualTo("'-department");
        assertThat(record.get("location")).isEqualTo("' @location");
        assertThat(record.get("salary_min")).isEqualTo("-42.50");
        assertThat(record.get("match_score")).isEqualTo("-9.5");
    }

    private CSVRecord parse(byte[] bytes) throws Exception {
        String csv = new String(bytes, StandardCharsets.UTF_8);
        return CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
            .get().parse(new StringReader(csv)).getRecords().getFirst();
    }

    private ApplicationTrackerResponse fullRow() {
        OffsetDateTime created = OffsetDateTime.parse("2026-07-01T10:15:30-04:00");
        return new ApplicationTrackerResponse(
            7L, 8L, "GitHub, Inc.", "Platform \"Lead\"", "Développeur",
            "New York", RemoteType.HYBRID, "Full-time",
            new BigDecimal("-500.25"), new BigDecimal("150000"), "USD",
            "salary notes", "https://example.test/job?a=1,b=2", "Referral",
            LocalDate.parse("2026-07-01"), LocalDate.parse("2026-08-01"),
            (short) 1, new BigDecimal("9.5"), "description", "job line 1\njob line 2",
            created, created.plusDays(1), 70L, ApplicationStatus.APPLIED,
            "v2", true, "https://portfolio.test", "https://github.test",
            "project", "skills", "topics", "Recruiter", "r@example.test",
            LocalDate.parse("2026-07-02"), LocalDate.parse("2026-07-09"),
            created.plusDays(2), created.plusDays(3), created.plusDays(4),
            created.plusDays(5), created.plusDays(6), "application notes",
            created.plusDays(1), created.plusDays(2)
        );
    }
}
