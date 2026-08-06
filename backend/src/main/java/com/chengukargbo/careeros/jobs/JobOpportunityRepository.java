package com.chengukargbo.careeros.jobs;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobOpportunityRepository
    extends JpaRepository<JobOpportunity, Long> {

    List<JobOpportunity> findAllByOrderByPriorityAscCreatedAtDesc();

    List<JobOpportunity>
        findByPositionTitleContainingIgnoreCaseOrderByPriorityAscCreatedAtDesc(
            String positionTitle
        );
}