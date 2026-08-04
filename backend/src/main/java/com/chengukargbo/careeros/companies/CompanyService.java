package com.chengukargbo.careeros.companies;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.companies.dto.CompanyRequest;
import com.chengukargbo.careeros.companies.dto.CompanyResponse;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyResponse create(CompanyRequest request) {
        Company company = new Company(
            request.name().trim(),
            normalize(request.websiteUrl()),
            normalize(request.careersUrl()),
            normalize(request.industry()),
            normalize(request.companyType()),
            normalize(request.mission()),
            normalize(request.products()),
            normalize(request.techStack()),
            normalize(request.remotePolicy()),
            normalize(request.salaryNotes()),
            normalize(request.generalNotes()),
            request.dreamCompany()
        );

        Company savedCompany = companyRepository.save(company);

        return CompanyResponse.from(savedCompany);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll()
            .stream()
            .map(CompanyResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new CompanyNotFoundException(id));

        return CompanyResponse.from(company);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}