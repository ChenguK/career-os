package com.chengukargbo.careeros.companies;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    Optional<Company> findFirstByNameIgnoreCase(String name);
}
