package com.chengukargbo.careeros.materials;

import java.time.OffsetDateTime;

import com.chengukargbo.careeros.profile.ApplicantProfile;
import jakarta.persistence.*;

@Entity
@Table(name = "career_materials")
public class CareerMaterial {
    public enum MaterialType { RESUME }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_profile_id", nullable = false)
    private ApplicantProfile applicantProfile;
    @Enumerated(EnumType.STRING) @Column(name="material_type",nullable=false,length=30)
    private MaterialType materialType;
    @Column(name="display_name",nullable=false,length=200) private String displayName;
    @Column(name="original_filename",nullable=false,length=255) private String originalFilename;
    @Column(name="storage_key",nullable=false,unique=true,length=255) private String storageKey;
    @Column(name="mime_type",nullable=false,length=120) private String mimeType;
    @Column(name="file_size",nullable=false) private long fileSize;
    @Column(nullable=false) private boolean active;
    @Column(columnDefinition="TEXT") private String notes;
    @Column(name="target_job_family",length=120) private String targetJobFamily;
    @Column(name="target_seniority",length=80) private String targetSeniority;
    @Column(name="version_label",length=100) private String versionLabel;
    @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
    @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;

    protected CareerMaterial() {}

    CareerMaterial(ApplicantProfile profile, String displayName, String originalFilename,
        String storageKey, String mimeType, long fileSize, String notes,
        String targetJobFamily, String targetSeniority, String versionLabel) {
        this.applicantProfile=profile; this.materialType=MaterialType.RESUME;
        this.displayName=displayName; this.originalFilename=originalFilename;
        this.storageKey=storageKey; this.mimeType=mimeType; this.fileSize=fileSize;
        this.notes=notes; this.targetJobFamily=targetJobFamily;
        this.targetSeniority=targetSeniority; this.versionLabel=versionLabel;
        this.active=true;
    }

    void deactivate(){active=false;}
    @PrePersist void create(){createdAt=updatedAt=OffsetDateTime.now();}
    @PreUpdate void updateTimestamp(){updatedAt=OffsetDateTime.now();}
    public Long getId(){return id;} public ApplicantProfile getApplicantProfile(){return applicantProfile;}
    public MaterialType getMaterialType(){return materialType;} public String getDisplayName(){return displayName;}
    public String getOriginalFilename(){return originalFilename;} public String getStorageKey(){return storageKey;}
    public String getMimeType(){return mimeType;} public long getFileSize(){return fileSize;}
    public boolean isActive(){return active;} public String getNotes(){return notes;}
    public String getTargetJobFamily(){return targetJobFamily;} public String getTargetSeniority(){return targetSeniority;}
    public String getVersionLabel(){return versionLabel;} public OffsetDateTime getCreatedAt(){return createdAt;}
    public OffsetDateTime getUpdatedAt(){return updatedAt;}
}
