package com.chengukargbo.careeros.companies;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CompanyMatchingService {

    private final CompanyRepository companyRepository;

    public CompanyMatchingService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Optional<Company> findNormalizedMatch(String companyName) {
        String displayName = trimToNull(companyName);
        if (displayName == null) {
            return Optional.empty();
        }
        return companyRepository.findFirstByNameIgnoreCase(displayName);
    }

    public String normalizeForComparison(String companyName) {
        String displayName = trimToNull(companyName);
        return displayName == null
            ? null
            : displayName.toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
