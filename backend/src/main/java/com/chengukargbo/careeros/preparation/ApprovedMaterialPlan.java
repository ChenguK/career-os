package com.chengukargbo.careeros.preparation;

import java.time.OffsetDateTime;
import com.chengukargbo.careeros.materials.CareerMaterial;
import jakarta.persistence.*;

@Entity @Table(name="approved_material_plans")
public class ApprovedMaterialPlan {
    public enum AuthoritySource { APPLICATION_SELECTION, PROFILE_DEFAULT }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="session_id",unique=true,nullable=false)
    private ApplicationPreparationSession session;
    @Enumerated(EnumType.STRING) @Column(name="material_type",nullable=false,length=30)
    private CareerMaterial.MaterialType materialType;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="career_material_id",nullable=false)
    private CareerMaterial careerMaterial;
    @Enumerated(EnumType.STRING) @Column(name="authority_source",nullable=false,length=40)
    private AuthoritySource authoritySource;
    @Column(name="display_filename",nullable=false,length=255) private String displayFilename;
    @Column(name="mime_type",nullable=false,length=120) private String mimeType;
    @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
    protected ApprovedMaterialPlan(){}
    ApprovedMaterialPlan(ApplicationPreparationSession session,CareerMaterial material,AuthoritySource source){this.session=session;this.careerMaterial=material;this.materialType=material.getMaterialType();this.authoritySource=source;this.displayFilename=material.getOriginalFilename();this.mimeType=material.getMimeType();}
    @PrePersist void create(){createdAt=OffsetDateTime.now();}
    public Long getId(){return id;} public ApplicationPreparationSession getSession(){return session;}
    public CareerMaterial getCareerMaterial(){return careerMaterial;} public AuthoritySource getAuthoritySource(){return authoritySource;}
    public String getDisplayFilename(){return displayFilename;} public String getMimeType(){return mimeType;}
    public OffsetDateTime getCreatedAt(){return createdAt;}
}
