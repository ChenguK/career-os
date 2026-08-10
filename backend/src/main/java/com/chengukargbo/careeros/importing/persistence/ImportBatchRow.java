package com.chengukargbo.careeros.importing.persistence;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "import_batch_rows")
public class ImportBatchRow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "import_batch_id", nullable = false)
    private Long importBatchId;
    @Column(name = "source_row_number", nullable = false)
    private int sourceRowNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private ImportRowOutcomeStatus outcome;
    @Column(name = "company_id") private Long companyId;
    @Column(name = "job_opportunity_id") private Long jobOpportunityId;
    @Column(name = "application_id") private Long applicationId;
    @Column(name = "duplicate_job_opportunity_id")
    private Long duplicateJobOpportunityId;
    @Column(columnDefinition = "TEXT") private String warnings;
    @Column(columnDefinition = "TEXT") private String errors;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ImportBatchRow() {}

    public ImportBatchRow(Long batchId, ImportRowPersistenceResult result) {
        importBatchId = batchId;
        sourceRowNumber = result.rowNumber();
        outcome = result.status();
        companyId = result.companyId();
        jobOpportunityId = result.jobOpportunityId();
        applicationId = result.applicationId();
        duplicateJobOpportunityId = result.duplicateJobOpportunityId();
        warnings = String.join("\n", result.warnings());
        errors = String.join("\n", result.errors());
    }

    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public Long getImportBatchId() { return importBatchId; }
    public int getSourceRowNumber() { return sourceRowNumber; }
    public ImportRowOutcomeStatus getOutcome() { return outcome; }
    public Long getCompanyId() { return companyId; }
    public Long getJobOpportunityId() { return jobOpportunityId; }
    public Long getApplicationId() { return applicationId; }
    public Long getDuplicateJobOpportunityId() {
        return duplicateJobOpportunityId;
    }
    public String getWarnings() { return warnings; }
    public String getErrors() { return errors; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
