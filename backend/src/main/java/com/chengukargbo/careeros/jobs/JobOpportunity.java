package com.chengukargbo.careeros.jobs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.companies.Company;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_opportunities")
public class JobOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "position_title", nullable = false, length = 200)
    private String positionTitle;

    @Column(length = 150)
    private String department;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "remote_type", nullable = false, length = 30)
    private RemoteType remoteType;

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "salary_currency", nullable = false, length = 3)
    private String salaryCurrency;

    @Column(name = "salary_notes", columnDefinition = "TEXT")
    private String salaryNotes;

    @Column(name = "application_url", length = 1000)
    private String applicationUrl;

    @Column(length = 150)
    private String source;

    @Column(name = "date_posted")
    private LocalDate datePosted;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(nullable = false)
    private short priority;

    @Column(name = "match_score", precision = 3, scale = 1)
    private BigDecimal matchScore;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected JobOpportunity() {
    }

    public JobOpportunity(
        Company company,
        String positionTitle,
        String department,
        String location,
        RemoteType remoteType,
        String employmentType,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String salaryCurrency,
        String salaryNotes,
        String applicationUrl,
        String source,
        LocalDate datePosted,
        LocalDate closingDate,
        short priority,
        BigDecimal matchScore,
        String jobDescription,
        String notes
    ) {
        this.company = company;
        this.positionTitle = positionTitle;
        this.department = department;
        this.location = location;
        this.remoteType = remoteType;
        this.employmentType = employmentType;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryCurrency = salaryCurrency;
        this.salaryNotes = salaryNotes;
        this.applicationUrl = applicationUrl;
        this.source = source;
        this.datePosted = datePosted;
        this.closingDate = closingDate;
        this.priority = priority;
        this.matchScore = matchScore;
        this.jobDescription = jobDescription;
        this.notes = notes;
    }
    public void update(
    Company company,
    String positionTitle,
    String department,
    String location,
    RemoteType remoteType,
    String employmentType,
    BigDecimal salaryMin,
    BigDecimal salaryMax,
    String salaryCurrency,
    String salaryNotes,
    String applicationUrl,
    String source,
    LocalDate datePosted,
    LocalDate closingDate,
    short priority,
    BigDecimal matchScore,
    String jobDescription,
    String notes
    ) {
        this.company = company;
        this.positionTitle = positionTitle;
        this.department = department;
        this.location = location;
        this.remoteType = remoteType;
        this.employmentType = employmentType;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryCurrency = salaryCurrency;
        this.salaryNotes = salaryNotes;
        this.applicationUrl = applicationUrl;
        this.source = source;
        this.datePosted = datePosted;
        this.closingDate = closingDate;
        this.priority = priority;
        this.matchScore = matchScore;
        this.jobDescription = jobDescription;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public String getDepartment() {
        return department;
    }

    public String getLocation() {
        return location;
    }

    public RemoteType getRemoteType() {
        return remoteType;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public BigDecimal getSalaryMin() {
        return salaryMin;
    }

    public BigDecimal getSalaryMax() {
        return salaryMax;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public String getSalaryNotes() {
        return salaryNotes;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public String getSource() {
        return source;
    }

    public LocalDate getDatePosted() {
        return datePosted;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public short getPriority() {
        return priority;
    }

    public BigDecimal getMatchScore() {
        return matchScore;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getNotes() {
        return notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}