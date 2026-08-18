package com.chengukargbo.careeros.applications.export;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;

@Component
public class ApplicationTrackerCsvSerializer {

    public static final String[] HEADERS = {
        "job_id", "company_id", "company_name", "position_title",
        "department", "location", "work_arrangement", "employment_type",
        "salary_min", "salary_max", "salary_currency", "salary_notes",
        "application_url", "source", "date_posted", "closing_date",
        "priority", "match_score", "job_description", "job_notes",
        "job_created_at", "job_updated_at", "application_id", "status",
        "resume_version", "cover_letter_needed", "portfolio_link",
        "github_link", "projects_to_highlight", "skills_to_emphasize",
        "interview_topics", "recruiter_name", "recruiter_email",
        "application_date", "follow_up_date", "phone_screen_at",
        "interview_one_at", "interview_two_at", "offer_at", "rejected_at",
        "application_notes", "application_created_at",
        "application_updated_at"
    };

    public byte[] serialize(List<ApplicationTrackerResponse> rows) {
        StringWriter output = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
            .setHeader(HEADERS)
            .setRecordSeparator("\r\n")
            .get();

        try (CSVPrinter csv = new CSVPrinter(output, format)) {
            for (ApplicationTrackerResponse row : rows) {
                csv.printRecord(values(row));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create tracker CSV", exception);
        }

        return output.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Object[] values(ApplicationTrackerResponse row) {
        return new Object[] {
            row.jobOpportunityId(), row.companyId(), text(row.companyName()),
            text(row.positionTitle()), text(row.department()),
            text(row.location()), value(row.remoteType()),
            value(row.employmentType()), decimal(row.salaryMin()),
            decimal(row.salaryMax()), text(row.salaryCurrency()),
            text(row.salaryNotes()), text(row.applicationUrl()),
            text(row.source()), value(row.datePosted()),
            value(row.closingDate()), row.priority(), row.matchScore(),
            text(row.jobDescription()), text(row.jobNotes()),
            datetime(row.jobCreatedAt()), datetime(row.jobUpdatedAt()),
            row.applicationId(),
            row.status() == null ? ApplicationStatus.SAVED : row.status(),
            text(row.resumeVersion()), row.coverLetterNeeded(),
            text(row.portfolioLink()), text(row.githubLink()),
            text(row.projectsToHighlight()), text(row.skillsToEmphasize()),
            text(row.interviewTopics()), text(row.recruiterName()),
            text(row.recruiterEmail()), value(row.applicationDate()),
            value(row.followUpDate()), datetime(row.phoneScreenAt()),
            datetime(row.interviewOneAt()), datetime(row.interviewTwoAt()),
            datetime(row.offerAt()), datetime(row.rejectedAt()),
            text(row.applicationNotes()), datetime(row.applicationCreatedAt()),
            datetime(row.applicationUpdatedAt())
        };
    }

    private Object value(Object value) {
        return value == null ? "" : value;
    }

    private Object decimal(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private Object datetime(OffsetDateTime value) {
        return value == null
            ? ""
            : value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String text(String value) {
        if (value == null) {
            return "";
        }

        int index = 0;
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        if (index < value.length() && "=+-@".indexOf(value.charAt(index)) >= 0) {
            return "'" + value;
        }
        return value;
    }
}
