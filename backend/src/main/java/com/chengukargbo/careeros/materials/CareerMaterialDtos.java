package com.chengukargbo.careeros.materials;

import java.time.OffsetDateTime;
import com.chengukargbo.careeros.materials.CareerMaterial.MaterialType;

public final class CareerMaterialDtos {
    private CareerMaterialDtos() {}
    public record Response(Long id, Long applicantProfileId, MaterialType materialType,
        String displayName, String originalFilename, String mimeType, long fileSize,
        boolean active, String notes, String targetJobFamily, String targetSeniority,
        String versionLabel, boolean profileDefault, OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}
}
