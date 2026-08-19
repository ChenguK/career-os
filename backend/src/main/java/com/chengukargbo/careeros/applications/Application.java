package com.chengukargbo.careeros.applications;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.materials.CareerMaterial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "job_opportunity_id",
        nullable = false,
        unique = true
    )
    private JobOpportunity jobOpportunity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ApplicationStatus status;

    @Column(name = "resume_version", length = 100)
    private String resumeVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_material_id")
    private CareerMaterial resumeMaterial;

    @Column(name = "cover_letter_needed", nullable = false)
    private boolean coverLetterNeeded;

    @Column(name = "portfolio_link", length = 1000)
    private String portfolioLink;

    @Column(name = "github_link", length = 1000)
    private String githubLink;

    @Column(name = "projects_to_highlight", columnDefinition = "TEXT")
    private String projectsToHighlight;

    @Column(name = "skills_to_emphasize", columnDefinition = "TEXT")
    private String skillsToEmphasize;

    @Column(name = "interview_topics", columnDefinition = "TEXT")
    private String interviewTopics;

    @Column(name = "recruiter_name", length = 200)
    private String recruiterName;

    @Column(name = "recruiter_email", length = 320)
    private String recruiterEmail;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "phone_screen_at")
    private OffsetDateTime phoneScreenAt;

    @Column(name = "interview_one_at")
    private OffsetDateTime interviewOneAt;

    @Column(name = "interview_two_at")
    private OffsetDateTime interviewTwoAt;

    @Column(name = "offer_at")
    private OffsetDateTime offerAt;

    @Column(name = "rejected_at")
    private OffsetDateTime rejectedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Application() {
    }

    public Application(JobOpportunity jobOpportunity, ApplicationStatus status,
        String resumeVersion, boolean coverLetterNeeded, String portfolioLink,
        String githubLink, String projectsToHighlight, String skillsToEmphasize,
        String interviewTopics, String recruiterName, String recruiterEmail,
        LocalDate applicationDate, LocalDate followUpDate, OffsetDateTime phoneScreenAt,
        OffsetDateTime interviewOneAt, OffsetDateTime interviewTwoAt, OffsetDateTime offerAt,
        OffsetDateTime rejectedAt, String notes) {
        this(jobOpportunity,status,resumeVersion,null,coverLetterNeeded,portfolioLink,
            githubLink,projectsToHighlight,skillsToEmphasize,interviewTopics,recruiterName,
            recruiterEmail,applicationDate,followUpDate,phoneScreenAt,interviewOneAt,
            interviewTwoAt,offerAt,rejectedAt,notes);
    }

    public Application(
        JobOpportunity jobOpportunity,
        ApplicationStatus status,
        String resumeVersion,
        CareerMaterial resumeMaterial,
        boolean coverLetterNeeded,
        String portfolioLink,
        String githubLink,
        String projectsToHighlight,
        String skillsToEmphasize,
        String interviewTopics,
        String recruiterName,
        String recruiterEmail,
        LocalDate applicationDate,
        LocalDate followUpDate,
        OffsetDateTime phoneScreenAt,
        OffsetDateTime interviewOneAt,
        OffsetDateTime interviewTwoAt,
        OffsetDateTime offerAt,
        OffsetDateTime rejectedAt,
        String notes
    ) {
        this.jobOpportunity = jobOpportunity;
        this.status = status;
        this.resumeVersion = resumeVersion;
        this.resumeMaterial = resumeMaterial;
        this.coverLetterNeeded = coverLetterNeeded;
        this.portfolioLink = portfolioLink;
        this.githubLink = githubLink;
        this.projectsToHighlight = projectsToHighlight;
        this.skillsToEmphasize = skillsToEmphasize;
        this.interviewTopics = interviewTopics;
        this.recruiterName = recruiterName;
        this.recruiterEmail = recruiterEmail;
        this.applicationDate = applicationDate;
        this.followUpDate = followUpDate;
        this.phoneScreenAt = phoneScreenAt;
        this.interviewOneAt = interviewOneAt;
        this.interviewTwoAt = interviewTwoAt;
        this.offerAt = offerAt;
        this.rejectedAt = rejectedAt;
        this.notes = notes;
    }

    public void update(
        ApplicationStatus status,
        String resumeVersion,
        CareerMaterial resumeMaterial,
        boolean coverLetterNeeded,
        String portfolioLink,
        String githubLink,
        String projectsToHighlight,
        String skillsToEmphasize,
        String interviewTopics,
        String recruiterName,
        String recruiterEmail,
        LocalDate applicationDate,
        LocalDate followUpDate,
        OffsetDateTime phoneScreenAt,
        OffsetDateTime interviewOneAt,
        OffsetDateTime interviewTwoAt,
        OffsetDateTime offerAt,
        OffsetDateTime rejectedAt,
        String notes
    ) {
        this.status = status;
        this.resumeVersion = resumeVersion;
        this.resumeMaterial = resumeMaterial;
        this.coverLetterNeeded = coverLetterNeeded;
        this.portfolioLink = portfolioLink;
        this.githubLink = githubLink;
        this.projectsToHighlight = projectsToHighlight;
        this.skillsToEmphasize = skillsToEmphasize;
        this.interviewTopics = interviewTopics;
        this.recruiterName = recruiterName;
        this.recruiterEmail = recruiterEmail;
        this.applicationDate = applicationDate;
        this.followUpDate = followUpDate;
        this.phoneScreenAt = phoneScreenAt;
        this.interviewOneAt = interviewOneAt;
        this.interviewTwoAt = interviewTwoAt;
        this.offerAt = offerAt;
        this.rejectedAt = rejectedAt;
        this.notes = notes;
    }

    public void update(ApplicationStatus status,String resumeVersion,boolean coverLetterNeeded,
        String portfolioLink,String githubLink,String projectsToHighlight,String skillsToEmphasize,
        String interviewTopics,String recruiterName,String recruiterEmail,LocalDate applicationDate,
        LocalDate followUpDate,OffsetDateTime phoneScreenAt,OffsetDateTime interviewOneAt,
        OffsetDateTime interviewTwoAt,OffsetDateTime offerAt,OffsetDateTime rejectedAt,String notes){
        update(status,resumeVersion,resumeMaterial,coverLetterNeeded,portfolioLink,githubLink,
            projectsToHighlight,skillsToEmphasize,interviewTopics,recruiterName,recruiterEmail,
            applicationDate,followUpDate,phoneScreenAt,interviewOneAt,interviewTwoAt,offerAt,rejectedAt,notes);
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

    public JobOpportunity getJobOpportunity() {
        return jobOpportunity;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getResumeVersion() {
        return resumeVersion;
    }

    public CareerMaterial getResumeMaterial() { return resumeMaterial; }

    public boolean isCoverLetterNeeded() {
        return coverLetterNeeded;
    }

    public String getPortfolioLink() {
        return portfolioLink;
    }

    public String getGithubLink() {
        return githubLink;
    }

    public String getProjectsToHighlight() {
        return projectsToHighlight;
    }

    public String getSkillsToEmphasize() {
        return skillsToEmphasize;
    }

    public String getInterviewTopics() {
        return interviewTopics;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public String getRecruiterEmail() {
        return recruiterEmail;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    void recordManualSubmission(LocalDate confirmedApplicationDate) {
        this.status = ApplicationStatus.APPLIED;
        this.applicationDate = confirmedApplicationDate;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public OffsetDateTime getPhoneScreenAt() {
        return phoneScreenAt;
    }

    public OffsetDateTime getInterviewOneAt() {
        return interviewOneAt;
    }

    public OffsetDateTime getInterviewTwoAt() {
        return interviewTwoAt;
    }

    public OffsetDateTime getOfferAt() {
        return offerAt;
    }

    public OffsetDateTime getRejectedAt() {
        return rejectedAt;
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
