package com.chengukargbo.careeros.importing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;

@Service
@Transactional(readOnly = true)
public class ImportDuplicateAnalyzer {

    private final JobOpportunityRepository jobRepository;
    private final ApplicationUrlNormalizer urlNormalizer;

    public ImportDuplicateAnalyzer(
        JobOpportunityRepository jobRepository,
        ApplicationUrlNormalizer urlNormalizer
    ) {
        this.jobRepository = jobRepository;
        this.urlNormalizer = urlNormalizer;
    }

    public List<ImportRowResult> analyze(List<ImportRowResult> rows) {
        List<JobOpportunity> persistedJobs = jobRepository.findAll();
        Map<String, ImportDuplicateMatch> persistedByUrl = new HashMap<>();
        Map<String, List<ImportDuplicateMatch>> persistedByCompanyTitle =
            new HashMap<>();

        persistedJobs.forEach(job -> {
            ImportDuplicateMatch match = persistedMatch(job);
            String normalizedUrl = urlNormalizer.normalize(
                job.getApplicationUrl()
            );
            if (normalizedUrl != null) {
                persistedByUrl.putIfAbsent(normalizedUrl, match);
            }
            String key = companyTitleKey(
                match.companyName(), match.positionTitle()
            );
            if (key != null) {
                persistedByCompanyTitle
                    .computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(match);
            }
        });

        Map<String, ImportDuplicateMatch> priorRowsByUrl = new HashMap<>();
        Map<String, List<ImportDuplicateMatch>> priorRowsByCompanyTitle =
            new HashMap<>();
        List<ImportRowResult> analyzed = new ArrayList<>();

        for (ImportRowResult row : rows) {
            analyzed.add(analyzeRow(
                row,
                persistedByUrl,
                persistedByCompanyTitle,
                priorRowsByUrl,
                priorRowsByCompanyTitle
            ));
            remember(row, priorRowsByUrl, priorRowsByCompanyTitle);
        }
        return List.copyOf(analyzed);
    }

    private ImportRowResult analyzeRow(
        ImportRowResult row,
        Map<String, ImportDuplicateMatch> persistedByUrl,
        Map<String, List<ImportDuplicateMatch>> persistedByCompanyTitle,
        Map<String, ImportDuplicateMatch> priorRowsByUrl,
        Map<String, List<ImportDuplicateMatch>> priorRowsByCompanyTitle
    ) {
        List<ImportIssue> warnings = new ArrayList<>(row.warnings());
        String normalizedUrl = row.normalizedApplicationUrl();
        ImportDuplicateMatch exactUrlDuplicate = normalizedUrl == null
            ? null
            : persistedByUrl.get(normalizedUrl);
        if (exactUrlDuplicate == null && normalizedUrl != null) {
            exactUrlDuplicate = priorRowsByUrl.get(normalizedUrl);
        }

        String companyTitleKey = companyTitleKey(
            row.values().companyName(), row.values().positionTitle()
        );
        List<ImportDuplicateMatch> candidates = new ArrayList<>();
        if (companyTitleKey != null) {
            candidates.addAll(persistedByCompanyTitle.getOrDefault(
                companyTitleKey, List.of()
            ));
            candidates.addAll(priorRowsByCompanyTitle.getOrDefault(
                companyTitleKey, List.of()
            ));
        }

        if (exactUrlDuplicate != null) {
            warnings.add(new ImportIssue(
                "application_url", "Application URL matches another job"
            ));
        }
        if (!candidates.isEmpty()) {
            warnings.add(new ImportIssue(
                "position_title", "Company and position title match another job"
            ));
        }

        ImportProposedAction action;
        boolean selectable;
        if (!row.errors().isEmpty()) {
            action = ImportProposedAction.INVALID;
            selectable = false;
        } else if (exactUrlDuplicate != null) {
            action = ImportProposedAction.SKIP_DUPLICATE;
            selectable = false;
        } else if (!candidates.isEmpty()) {
            action = ImportProposedAction.REVIEW_WARNING;
            selectable = true;
        } else {
            action = ImportProposedAction.CREATE;
            selectable = true;
        }

        return new ImportRowResult(
            row.rowNumber(),
            row.values(),
            row.errors(),
            warnings,
            normalizedUrl,
            exactUrlDuplicate,
            candidates,
            action,
            selectable
        );
    }

    private void remember(
        ImportRowResult row,
        Map<String, ImportDuplicateMatch> priorRowsByUrl,
        Map<String, List<ImportDuplicateMatch>> priorRowsByCompanyTitle
    ) {
        ImportDuplicateMatch match = new ImportDuplicateMatch(
            null,
            row.rowNumber(),
            row.values().companyName(),
            row.values().positionTitle(),
            row.values().applicationUrl()
        );
        if (row.normalizedApplicationUrl() != null) {
            priorRowsByUrl.putIfAbsent(row.normalizedApplicationUrl(), match);
        }
        String key = companyTitleKey(
            row.values().companyName(), row.values().positionTitle()
        );
        if (key != null) {
            priorRowsByCompanyTitle
                .computeIfAbsent(key, ignored -> new ArrayList<>())
                .add(match);
        }
    }

    private ImportDuplicateMatch persistedMatch(JobOpportunity job) {
        return new ImportDuplicateMatch(
            job.getId(),
            null,
            job.getCompany() == null ? null : job.getCompany().getName(),
            job.getPositionTitle(),
            job.getApplicationUrl()
        );
    }

    private String companyTitleKey(String company, String title) {
        String normalizedCompany = comparisonValue(company);
        String normalizedTitle = comparisonValue(title);
        return normalizedCompany == null || normalizedTitle == null
            ? null
            : normalizedCompany + "\u0000" + normalizedTitle;
    }

    private String comparisonValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty()
            ? null
            : trimmed.toLowerCase(Locale.ROOT);
    }
}
