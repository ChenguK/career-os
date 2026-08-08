package com.chengukargbo.careeros.applications;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository
    extends JpaRepository<Application, Long> {

    boolean existsByJobOpportunityId(Long jobOpportunityId);

    List<Application>
        findAllByOrderByUpdatedAtDesc();
}