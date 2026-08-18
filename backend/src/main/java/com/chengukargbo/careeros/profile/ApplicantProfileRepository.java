package com.chengukargbo.careeros.profile;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantProfileRepository
    extends JpaRepository<ApplicantProfile, Long> {

    Optional<ApplicantProfile> findByProfileKey(String profileKey);
}
