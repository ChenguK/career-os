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

    public CompanyResponse update(Long id, CompanyRequest request) {
    Company company = findEntityById(id);

    company.update(
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

    Company updatedCompany = companyRepository.saveAndFlush(company);

    return CompanyResponse.from(updatedCompany);
    }

    public void delete(Long id) {
        Company company = findEntityById(id);
        companyRepository.delete(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> search(String searchTerm) {
        String normalizedSearch = normalize(searchTerm);

        if (normalizedSearch == null) {
            return findAll();
        }

        return companyRepository
            .findByNameContainingIgnoreCaseOrderByNameAsc(normalizedSearch)
            .stream()
            .map(CompanyResponse::from)
            .toList();
    }

    private Company findEntityById(Long id) {
        return companyRepository.findById(id)
            .orElseThrow(() -> new CompanyNotFoundException(id));
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
        return CompanyResponse.from(findEntityById(id));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}