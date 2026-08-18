package com.chengukargbo.careeros.profile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

import com.chengukargbo.careeros.jobs.RemoteType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "applicant_profiles")
public class ApplicantProfile {

    // V1 has one local owner. A future authenticated owner ID replaces this
    // lookup key without coupling profile ownership to jobs or applications.
    public static final String PRIMARY_PROFILE_KEY = "PRIMARY";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_key", nullable = false, unique = true, length = 40)
    private String profileKey;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "preferred_name", length = 100)
    private String preferredName;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 100)
    private String city;

    @Column(name = "state_region", length = 100)
    private String stateRegion;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "portfolio_url", length = 1000)
    private String portfolioUrl;

    @Column(name = "github_url", length = 1000)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 1000)
    private String linkedinUrl;

    @Column(name = "default_resume_version", length = 100)
    private String defaultResumeVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_work_arrangement", nullable = false, length = 30)
    private RemoteType preferredWorkArrangement;

    @Column(name = "minimum_salary", precision = 12, scale = 2)
    private BigDecimal minimumSalary;

    @Column(name = "salary_currency", nullable = false, length = 3)
    private String salaryCurrency;

    @Column(name = "willing_to_relocate")
    private Boolean willingToRelocate;

    @Column(name = "willing_to_travel")
    private Boolean willingToTravel;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "last_verified_at")
    private OffsetDateTime lastVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ApplicantProfile() {
    }

    public ApplicantProfile(
        String firstName,
        String lastName,
        String preferredName,
        String email,
        String phone,
        String city,
        String stateRegion,
        String country,
        String postalCode,
        String portfolioUrl,
        String githubUrl,
        String linkedinUrl,
        String defaultResumeVersion,
        RemoteType preferredWorkArrangement,
        BigDecimal minimumSalary,
        String salaryCurrency,
        Boolean willingToRelocate,
        Boolean willingToTravel
    ) {
        profileKey = PRIMARY_PROFILE_KEY;
        apply(
            firstName, lastName, preferredName, email, phone, city,
            stateRegion, country, postalCode, portfolioUrl, githubUrl,
            linkedinUrl, defaultResumeVersion, preferredWorkArrangement,
            minimumSalary, salaryCurrency, willingToRelocate,
            willingToTravel
        );
    }

    public void update(
        String firstName,
        String lastName,
        String preferredName,
        String email,
        String phone,
        String city,
        String stateRegion,
        String country,
        String postalCode,
        String portfolioUrl,
        String githubUrl,
        String linkedinUrl,
        String defaultResumeVersion,
        RemoteType preferredWorkArrangement,
        BigDecimal minimumSalary,
        String salaryCurrency,
        Boolean willingToRelocate,
        Boolean willingToTravel
    ) {
        boolean changed = !sameValues(
            firstName, lastName, preferredName, email, phone, city,
            stateRegion, country, postalCode, portfolioUrl, githubUrl,
            linkedinUrl, defaultResumeVersion, preferredWorkArrangement,
            minimumSalary, salaryCurrency, willingToRelocate,
            willingToTravel
        );
        apply(
            firstName, lastName, preferredName, email, phone, city,
            stateRegion, country, postalCode, portfolioUrl, githubUrl,
            linkedinUrl, defaultResumeVersion, preferredWorkArrangement,
            minimumSalary, salaryCurrency, willingToRelocate,
            willingToTravel
        );
        if (changed) {
            verified = false;
            lastVerifiedAt = null;
        }
    }

    public void verify() {
        verified = true;
        lastVerifiedAt = OffsetDateTime.now();
    }

    private void apply(
        String firstName, String lastName, String preferredName, String email,
        String phone, String city, String stateRegion, String country,
        String postalCode, String portfolioUrl, String githubUrl,
        String linkedinUrl, String defaultResumeVersion,
        RemoteType preferredWorkArrangement, BigDecimal minimumSalary,
        String salaryCurrency, Boolean willingToRelocate,
        Boolean willingToTravel
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.preferredName = preferredName;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.stateRegion = stateRegion;
        this.country = country;
        this.postalCode = postalCode;
        this.portfolioUrl = portfolioUrl;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.defaultResumeVersion = defaultResumeVersion;
        this.preferredWorkArrangement = preferredWorkArrangement;
        this.minimumSalary = minimumSalary;
        this.salaryCurrency = salaryCurrency;
        this.willingToRelocate = willingToRelocate;
        this.willingToTravel = willingToTravel;
    }

    private boolean sameValues(
        String firstName, String lastName, String preferredName, String email,
        String phone, String city, String stateRegion, String country,
        String postalCode, String portfolioUrl, String githubUrl,
        String linkedinUrl, String defaultResumeVersion,
        RemoteType preferredWorkArrangement, BigDecimal minimumSalary,
        String salaryCurrency, Boolean willingToRelocate,
        Boolean willingToTravel
    ) {
        return Objects.equals(this.firstName, firstName)
            && Objects.equals(this.lastName, lastName)
            && Objects.equals(this.preferredName, preferredName)
            && Objects.equals(this.email, email)
            && Objects.equals(this.phone, phone)
            && Objects.equals(this.city, city)
            && Objects.equals(this.stateRegion, stateRegion)
            && Objects.equals(this.country, country)
            && Objects.equals(this.postalCode, postalCode)
            && Objects.equals(this.portfolioUrl, portfolioUrl)
            && Objects.equals(this.githubUrl, githubUrl)
            && Objects.equals(this.linkedinUrl, linkedinUrl)
            && Objects.equals(this.defaultResumeVersion, defaultResumeVersion)
            && this.preferredWorkArrangement == preferredWorkArrangement
            && sameDecimal(this.minimumSalary, minimumSalary)
            && Objects.equals(this.salaryCurrency, salaryCurrency)
            && Objects.equals(this.willingToRelocate, willingToRelocate)
            && Objects.equals(this.willingToTravel, willingToTravel);
    }

    private boolean sameDecimal(BigDecimal first, BigDecimal second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.compareTo(second) == 0;
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

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPreferredName() { return preferredName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCity() { return city; }
    public String getStateRegion() { return stateRegion; }
    public String getCountry() { return country; }
    public String getPostalCode() { return postalCode; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public String getGithubUrl() { return githubUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public String getDefaultResumeVersion() { return defaultResumeVersion; }
    public RemoteType getPreferredWorkArrangement() {
        return preferredWorkArrangement;
    }
    public BigDecimal getMinimumSalary() { return minimumSalary; }
    public String getSalaryCurrency() { return salaryCurrency; }
    public Boolean getWillingToRelocate() { return willingToRelocate; }
    public Boolean getWillingToTravel() { return willingToTravel; }
    public boolean isVerified() { return verified; }
    public OffsetDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
