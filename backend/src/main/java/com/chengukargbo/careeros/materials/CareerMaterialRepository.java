package com.chengukargbo.careeros.materials;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerMaterialRepository extends JpaRepository<CareerMaterial,Long> {
    List<CareerMaterial> findByApplicantProfileIdOrderByActiveDescDisplayNameAscIdAsc(Long profileId);
}
