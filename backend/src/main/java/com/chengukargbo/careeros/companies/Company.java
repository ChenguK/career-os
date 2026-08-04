package com.chengukargbo.careeros.companies;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "careers_url", length = 500)
    private String careersUrl;

    @Column(length = 150)
    private String industry;

    @Column(name = "company_type", length = 100)
    private String companyType;

    @Column(columnDefinition = "TEXT")
    private String mission;

    @Column(columnDefinition = "TEXT")
    private String products;

    @Column(name = "tech_stack", columnDefinition = "TEXT")
    private String techStack;

    @Column(name = "remote_policy", columnDefinition = "TEXT")
    private String remotePolicy;

    @Column(name = "salary_notes", columnDefinition = "TEXT")
    private String salaryNotes;

    @Column(name = "general_notes", columnDefinition = "TEXT")
    private String generalNotes;

    @Column(name = "dream_company", nullable = false)
    private boolean dreamCompany;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void update(
    String name,
    String websiteUrl,
    String careersUrl,
    String industry,
    String companyType,
    String mission,
    String products,
    String techStack,
    String remotePolicy,
    String salaryNotes,
    String generalNotes,
    boolean dreamCompany
) {
    this.name = name;
    this.websiteUrl = websiteUrl;
    this.careersUrl = careersUrl;
    this.industry = industry;
    this.companyType = companyType;
    this.mission = mission;
    this.products = products;
    this.techStack = techStack;
    this.remotePolicy = remotePolicy;
    this.salaryNotes = salaryNotes;
    this.generalNotes = generalNotes;
    this.dreamCompany = dreamCompany;
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

    protected Company() {
    }

    public Company(
        String name,
        String websiteUrl,
        String careersUrl,
        String industry,
        String companyType,
        String mission,
        String products,
        String techStack,
        String remotePolicy,
        String salaryNotes,
        String generalNotes,
        boolean dreamCompany
    ) {
        this.name = name;
        this.websiteUrl = websiteUrl;
        this.careersUrl = careersUrl;
        this.industry = industry;
        this.companyType = companyType;
        this.mission = mission;
        this.products = products;
        this.techStack = techStack;
        this.remotePolicy = remotePolicy;
        this.salaryNotes = salaryNotes;
        this.generalNotes = generalNotes;
        this.dreamCompany = dreamCompany;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getCareersUrl() {
        return careersUrl;
    }

    public String getIndustry() {
        return industry;
    }

    public String getCompanyType() {
        return companyType;
    }

    public String getMission() {
        return mission;
    }

    public String getProducts() {
        return products;
    }

    public String getTechStack() {
        return techStack;
    }

    public String getRemotePolicy() {
        return remotePolicy;
    }

    public String getSalaryNotes() {
        return salaryNotes;
    }

    public String getGeneralNotes() {
        return generalNotes;
    }

    public boolean isDreamCompany() {
        return dreamCompany;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}