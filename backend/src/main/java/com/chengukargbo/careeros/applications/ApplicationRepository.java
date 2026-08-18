package com.chengukargbo.careeros.applications;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository
    extends JpaRepository<Application, Long> {

    boolean existsByJobOpportunityId(Long jobOpportunityId);

    Optional<Application> findByJobOpportunityId(Long jobOpportunityId);

    List<Application>
        findAllByOrderByUpdatedAtDesc();
}
