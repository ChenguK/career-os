package com.chengukargbo.careeros.answers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovedAnswerRepository
    extends JpaRepository<ApprovedAnswer, Long> {

    List<ApprovedAnswer> findAllByOrderByCanonicalKeyAsc();

    boolean existsByCanonicalKey(String canonicalKey);

    boolean existsByCanonicalKeyAndIdNot(String canonicalKey, Long id);
    Optional<ApprovedAnswer> findByCanonicalKey(String canonicalKey);
}
