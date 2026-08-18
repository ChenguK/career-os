package com.chengukargbo.careeros.importing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ImportHeaderMapper {

    private static final Set<String> CANONICAL_HEADERS = Set.of(
        "position_title", "company_name", "department", "location",
        "work_arrangement", "employment_type", "salary_min", "salary_max",
        "salary_currency", "salary_notes", "application_url", "source",
        "date_posted", "closing_date", "priority", "match_score",
        "job_description", "job_notes", "status", "resume_version",
        "cover_letter_needed", "portfolio_link", "github_link",
        "projects_to_highlight", "skills_to_emphasize", "interview_topics",
        "recruiter_name", "recruiter_email", "application_date",
        "follow_up_date", "phone_screen_at", "interview_one_at",
        "interview_two_at", "offer_at", "rejected_at", "application_notes"
    );

    private static final Set<String> EXPORT_ONLY_HEADERS = Set.of(
        "job_id", "company_id", "job_created_at", "job_updated_at",
        "application_id", "application_created_at", "application_updated_at"
    );

    private static final Map<String, String> ALIASES = Map.ofEntries(
        Map.entry("company", "company_name"),
        Map.entry("job_title", "position_title"),
        Map.entry("work_type", "work_arrangement"),
        Map.entry("remote_type", "work_arrangement"),
        Map.entry("apply_url", "application_url"),
        Map.entry("fit", "match_score")
    );

    public HeaderMappingResult map(Map<String, String> rawFields) {
        Map<String, String> fields = new LinkedHashMap<>();
        List<ImportIssue> errors = new ArrayList<>();
        List<ImportIssue> warnings = new ArrayList<>();

        rawFields.forEach((header, value) -> {
            String normalizedHeader = normalizeHeader(header);
            String canonical = CANONICAL_HEADERS.contains(normalizedHeader)
                ? normalizedHeader
                : ALIASES.get(normalizedHeader);

            if (EXPORT_ONLY_HEADERS.contains(normalizedHeader)) {
                return;
            } else if (canonical == null) {
                warnings.add(new ImportIssue(
                    header,
                    "Unknown import header was ignored"
                ));
            } else if (fields.containsKey(canonical)) {
                errors.add(new ImportIssue(
                    canonical,
                    "Multiple headers map to the same canonical field"
                ));
            } else {
                fields.put(canonical, value);
            }
        });

        return new HeaderMappingResult(fields, errors, warnings);
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        return header.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[\\s-]+", "_");
    }
}
