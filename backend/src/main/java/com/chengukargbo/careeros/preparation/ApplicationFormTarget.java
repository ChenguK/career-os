package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.applications.Application;
import com.chengukargbo.careeros.preparation.PreparationEnums.IdentitySource;

import jakarta.persistence.*;

@Entity
@Table(name = "application_form_targets")
public class ApplicationFormTarget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;
    @Column(name = "normalized_form_url", nullable = false, length = 1000)
    private String normalizedFormUrl;
    @Column(name = "external_requisition_id", length = 200)
    private String externalRequisitionId;
    @Column(name = "external_form_key", length = 200)
    private String externalFormKey;
    @Enumerated(EnumType.STRING) @Column(name = "identity_source", nullable = false, length = 30)
    private IdentitySource identitySource;
    @Column(name = "last_confirmed_at", nullable = false)
    private OffsetDateTime lastConfirmedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ApplicationFormTarget() {}

    ApplicationFormTarget(Application application, String normalizedFormUrl) {
        this.application = application;
        confirm(normalizedFormUrl);
        identitySource = IdentitySource.USER;
    }

    void confirm(String normalizedFormUrl) {
        this.normalizedFormUrl = normalizedFormUrl;
        lastConfirmedAt = OffsetDateTime.now();
    }

    void observeIdentity(String normalizedFormUrl,
        String externalRequisitionId, String externalFormKey) {
        confirm(normalizedFormUrl);
        this.externalRequisitionId = externalRequisitionId;
        this.externalFormKey = externalFormKey;
        identitySource = IdentitySource.ADAPTER;
    }

    @PrePersist void create() { createdAt = updatedAt = OffsetDateTime.now(); }
    @PreUpdate void update() { updatedAt = OffsetDateTime.now(); }

    public Long getId() { return id; }
    public Application getApplication() { return application; }
    public String getNormalizedFormUrl() { return normalizedFormUrl; }
    public String getExternalRequisitionId() { return externalRequisitionId; }
    public String getExternalFormKey() { return externalFormKey; }
    public IdentitySource getIdentitySource() { return identitySource; }
    public OffsetDateTime getLastConfirmedAt() { return lastConfirmedAt; }
}
