package com.chengukargbo.careeros.importing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.jobs.RemoteType;

@Component
public class ImportRowNormalizer {

    private static final DateTimeFormatter US_DATE =
        DateTimeFormatter.ofPattern("MM/dd/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final ImportHeaderMapper headerMapper;
    private final ApplicationUrlNormalizer urlNormalizer;

    public ImportRowNormalizer(
        ImportHeaderMapper headerMapper,
        ApplicationUrlNormalizer urlNormalizer
    ) {
        this.headerMapper = headerMapper;
        this.urlNormalizer = urlNormalizer;
    }

    public ImportRowResult normalize(
        int rowNumber,
        Map<String, String> rawFields
    ) {
        HeaderMappingResult mapped = headerMapper.map(rawFields);
        Map<String, String> fields = mapped.fields();
        List<ImportIssue> errors = new ArrayList<>(mapped.errors());
        List<ImportIssue> warnings = new ArrayList<>(mapped.warnings());

        String positionTitle = string(fields, "position_title", 200, errors);
        String companyName = string(fields, "company_name", 200, errors);
        String department = string(fields, "department", 150, errors);
        String location = string(fields, "location", 200, errors);
        RemoteType remoteType = parseRemoteType(
            fields.get("work_arrangement"), errors
        );
        String employmentType = string(
            fields, "employment_type", 50, errors
        );
        BigDecimal salaryMin = parseDecimal(
            fields.get("salary_min"), "salary_min", true, errors
        );
        BigDecimal salaryMax = parseDecimal(
            fields.get("salary_max"), "salary_max", true, errors
        );
        String salaryCurrency = string(
            fields, "salary_currency", 3, errors
        );
        if (salaryCurrency == null) {
            salaryCurrency = "USD";
        } else if (salaryCurrency.length() != 3) {
            errors.add(new ImportIssue(
                "salary_currency", "Salary currency must use a three-letter code"
            ));
        } else {
            salaryCurrency = salaryCurrency.toUpperCase(Locale.ROOT);
        }

        Short priority = parsePriority(fields.get("priority"), errors);
        BigDecimal matchScore = parseDecimal(
            fields.get("match_score"), "match_score", false, errors
        );
        if (matchScore != null
            && (matchScore.compareTo(BigDecimal.ZERO) < 0
                || matchScore.compareTo(BigDecimal.TEN) > 0)) {
            errors.add(new ImportIssue(
                "match_score", "Match score must be between 0 and 10"
            ));
        }
        if (salaryMin != null && salaryMax != null
            && salaryMax.compareTo(salaryMin) < 0) {
            errors.add(new ImportIssue(
                "salary_max", "Maximum salary cannot be below minimum salary"
            ));
        }

        LocalDate datePosted = parseDate(
            fields.get("date_posted"), "date_posted", errors
        );
        LocalDate closingDate = parseDate(
            fields.get("closing_date"), "closing_date", errors
        );
        if (datePosted != null && closingDate != null
            && closingDate.isBefore(datePosted)) {
            errors.add(new ImportIssue(
                "closing_date", "Closing date cannot be before date posted"
            ));
        }

        ApplicationStatus status = parseStatus(fields.get("status"), errors);
        Boolean coverLetterNeeded = parseBoolean(
            fields.get("cover_letter_needed"), errors
        );
        String recruiterEmail = string(
            fields, "recruiter_email", 320, errors
        );
        if (recruiterEmail != null
            && !recruiterEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            errors.add(new ImportIssue(
                "recruiter_email", "Recruiter email must be valid"
            ));
        }

        CanonicalImportRow values = new CanonicalImportRow(
            positionTitle,
            companyName,
            department,
            location,
            remoteType,
            employmentType,
            salaryMin,
            salaryMax,
            salaryCurrency,
            string(fields, "salary_notes", null, errors),
            string(fields, "application_url", 1000, errors),
            string(fields, "source", 150, errors),
            datePosted,
            closingDate,
            priority,
            matchScore,
            string(fields, "job_description", null, errors),
            string(fields, "job_notes", null, errors),
            status,
            string(fields, "resume_version", 100, errors),
            coverLetterNeeded,
            string(fields, "portfolio_link", 1000, errors),
            string(fields, "github_link", 1000, errors),
            string(fields, "projects_to_highlight", null, errors),
            string(fields, "skills_to_emphasize", null, errors),
            string(fields, "interview_topics", null, errors),
            string(fields, "recruiter_name", 200, errors),
            recruiterEmail,
            parseDate(fields.get("application_date"), "application_date", errors),
            parseDate(fields.get("follow_up_date"), "follow_up_date", errors),
            parseDateTime(fields.get("phone_screen_at"), "phone_screen_at", errors),
            parseDateTime(fields.get("interview_one_at"), "interview_one_at", errors),
            parseDateTime(fields.get("interview_two_at"), "interview_two_at", errors),
            parseDateTime(fields.get("offer_at"), "offer_at", errors),
            parseDateTime(fields.get("rejected_at"), "rejected_at", errors),
            string(fields, "application_notes", null, errors)
        );

        if (positionTitle == null) {
            errors.add(new ImportIssue(
                "position_title", "Position title is required"
            ));
        }

        String normalizedUrl = null;
        try {
            normalizedUrl = urlNormalizer.normalize(values.applicationUrl());
        } catch (com.chengukargbo.careeros.common.exception.BusinessValidationException exception) {
            errors.add(new ImportIssue("application_url", exception.getMessage()));
        }
        boolean valid = errors.isEmpty();
        return new ImportRowResult(
            rowNumber,
            values,
            errors,
            warnings,
            normalizedUrl,
            null,
            List.of(),
            valid ? ImportProposedAction.CREATE : ImportProposedAction.INVALID,
            valid
        );
    }

    private String string(
        Map<String, String> fields,
        String field,
        Integer maxLength,
        List<ImportIssue> errors
    ) {
        String value = trimToNull(fields.get(field));
        if (value != null && maxLength != null && value.length() > maxLength) {
            errors.add(new ImportIssue(
                field, "Value must not exceed " + maxLength + " characters"
            ));
        }
        return value;
    }

    private RemoteType parseRemoteType(
        String raw,
        List<ImportIssue> errors
    ) {
        String value = token(raw);
        if (value == null) {
            return RemoteType.UNKNOWN;
        }
        return switch (value) {
            case "REMOTE" -> RemoteType.REMOTE;
            case "HYBRID" -> RemoteType.HYBRID;
            case "ONSITE", "ON_SITE" -> RemoteType.ONSITE;
            case "UNKNOWN" -> RemoteType.UNKNOWN;
            default -> {
                errors.add(new ImportIssue(
                    "work_arrangement", "Unknown work arrangement: " + raw.trim()
                ));
                yield null;
            }
        };
    }

    private ApplicationStatus parseStatus(
        String raw,
        List<ImportIssue> errors
    ) {
        String value = token(raw);
        if (value == null) {
            return null;
        }
        value = switch (value) {
            case "INTERVIEW_1" -> "INTERVIEW_ONE";
            case "INTERVIEW_2" -> "INTERVIEW_TWO";
            default -> value;
        };
        try {
            return ApplicationStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            errors.add(new ImportIssue(
                "status", "Unknown application status: " + raw.trim()
            ));
            return null;
        }
    }

    private Boolean parseBoolean(String raw, List<ImportIssue> errors) {
        String value = trimToNull(raw);
        if (value == null) {
            return false;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true", "yes", "y", "1" -> true;
            case "false", "no", "n", "0" -> false;
            default -> {
                errors.add(new ImportIssue(
                    "cover_letter_needed", "Value must be true/false, yes/no, y/n, or 1/0"
                ));
                yield null;
            }
        };
    }

    private BigDecimal parseDecimal(
        String raw,
        String field,
        boolean salary,
        List<ImportIssue> errors
    ) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        String normalized = salary
            ? value.replace(",", "").replace("$", "")
            : value;
        try {
            BigDecimal number = new BigDecimal(normalized);
            if (salary && number.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(new ImportIssue(field, "Salary cannot be negative"));
            }
            return number;
        } catch (NumberFormatException exception) {
            errors.add(new ImportIssue(field, "Value must be a decimal number"));
            return null;
        }
    }

    private Short parsePriority(String raw, List<ImportIssue> errors) {
        String value = trimToNull(raw);
        if (value == null) {
            return 3;
        }
        try {
            short priority = Short.parseShort(value);
            if (priority < 1 || priority > 5) {
                errors.add(new ImportIssue(
                    "priority", "Priority must be between 1 and 5"
                ));
            }
            return priority;
        } catch (NumberFormatException exception) {
            errors.add(new ImportIssue("priority", "Priority must be an integer"));
            return null;
        }
    }

    private LocalDate parseDate(
        String raw,
        String field,
        List<ImportIssue> errors
    ) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        for (DateTimeFormatter formatter : List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            US_DATE
        )) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try only the next explicitly supported format.
            }
        }
        errors.add(new ImportIssue(
            field, "Date must use YYYY-MM-DD or MM/DD/YYYY"
        ));
        return null;
    }

    private OffsetDateTime parseDateTime(
        String raw,
        String field,
        List<ImportIssue> errors
    ) {
        String value = trimToNull(raw);
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException exception) {
            errors.add(new ImportIssue(
                field, "Datetime must be ISO-8601 and include an offset"
            ));
            return null;
        }
    }

    private String token(String raw) {
        String value = trimToNull(raw);
        return value == null
            ? null
            : value.toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
