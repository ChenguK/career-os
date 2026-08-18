package com.chengukargbo.careeros.importing.persistence;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "import_batches")
public class ImportBatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    @Column(nullable = false, length = 20)
    private String format;
    @Column(name = "schema_version", nullable = false, length = 100)
    private String schemaVersion;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
    @Column(name = "total_row_count", nullable = false)
    private int totalRowCount;
    @Column(name = "selected_row_count", nullable = false)
    private int selectedRowCount;
    @Column(name = "created_row_count", nullable = false)
    private int createdRowCount;
    @Column(name = "warning_created_row_count", nullable = false)
    private int warningCreatedRowCount;
    @Column(name = "skipped_duplicate_row_count", nullable = false)
    private int skippedDuplicateRowCount;
    @Column(name = "failed_row_count", nullable = false)
    private int failedRowCount;

    protected ImportBatch() {}

    public ImportBatch(String filename, int totalRows, int selectedRows) {
        this.originalFilename = filename;
        this.format = filename.toLowerCase().endsWith(".xlsx")
            ? "XLSX"
            : "CSV";
        this.schemaVersion = "careeros_job_import_v1";
        this.totalRowCount = totalRows;
        this.selectedRowCount = selectedRows;
    }

    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); }

    public void complete(int created, int warnings, int skipped, int failed) {
        createdRowCount = created;
        warningCreatedRowCount = warnings;
        skippedDuplicateRowCount = skipped;
        failedRowCount = failed;
        completedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public String getFormat() { return format; }
    public String getSchemaVersion() { return schemaVersion; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public int getTotalRowCount() { return totalRowCount; }
    public int getSelectedRowCount() { return selectedRowCount; }
    public int getCreatedRowCount() { return createdRowCount; }
    public int getWarningCreatedRowCount() { return warningCreatedRowCount; }
    public int getSkippedDuplicateRowCount() { return skippedDuplicateRowCount; }
    public int getFailedRowCount() { return failedRowCount; }
}
