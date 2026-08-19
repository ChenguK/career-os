package com.chengukargbo.careeros.preparation;

import com.chengukargbo.careeros.preparation.PreparationEnums.MaterialType;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "observed_material_requirements")
public class ObservedMaterialRequirement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "snapshot_id", nullable = false)
    private FormObservationSnapshot snapshot;
    @Column(name = "external_field_id", nullable = false, length = 200)
    private String externalFieldId;
    @Enumerated(EnumType.STRING) @Column(name = "material_type", nullable = false, length = 30)
    private MaterialType materialType;
    @Column(name = "field_label", nullable = false, length = 1000)
    private String fieldLabel;
    @Column(nullable = false)
    private boolean required;
    @Column(name = "accept_types", length = 1000)
    private String acceptTypes;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
    @Column(name = "page_key", nullable = false, length = 200)
    private String pageKey;

    protected ObservedMaterialRequirement() {}

    ObservedMaterialRequirement(FormObservationSnapshot snapshot, String externalFieldId,
        MaterialType materialType, String fieldLabel, boolean required, String acceptTypes,
        int displayOrder, String pageKey) {
        this.snapshot = snapshot;
        this.externalFieldId = externalFieldId;
        this.materialType = materialType;
        this.fieldLabel = fieldLabel;
        this.required = required;
        this.acceptTypes = acceptTypes;
        this.displayOrder = displayOrder;
        this.pageKey = pageKey;
    }

    public Long getId() { return id; }
    public FormObservationSnapshot getSnapshot() { return snapshot; }
    public String getExternalFieldId() { return externalFieldId; }
    public MaterialType getMaterialType() { return materialType; }
    public String getFieldLabel() { return fieldLabel; }
    public boolean isRequired() { return required; }
    public String getAcceptTypes() { return acceptTypes; }
    public int getDisplayOrder() { return displayOrder; }
    public String getPageKey() { return pageKey; }
}
