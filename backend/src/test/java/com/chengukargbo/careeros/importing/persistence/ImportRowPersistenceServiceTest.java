package com.chengukargbo.careeros.importing.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chengukargbo.careeros.applications.ApplicationService;
import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.applications.dto.ApplicationRequest;
import com.chengukargbo.careeros.applications.dto.ApplicationResponse;
import com.chengukargbo.careeros.companies.Company;
import com.chengukargbo.careeros.companies.CompanyRepository;
import com.chengukargbo.careeros.importing.ApplicationUrlNormalizer;
import com.chengukargbo.careeros.importing.ImportHeaderMapper;
import com.chengukargbo.careeros.importing.ImportIssue;
import com.chengukargbo.careeros.importing.ImportProposedAction;
import com.chengukargbo.careeros.importing.ImportRowNormalizer;
import com.chengukargbo.careeros.importing.ImportRowResult;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;
import com.chengukargbo.careeros.jobs.JobOpportunityService;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityRequest;
import com.chengukargbo.careeros.jobs.dto.JobOpportunityResponse;

class ImportRowPersistenceServiceTest {

    private CompanyRepository companies;
    private JobOpportunityRepository jobs;
    private JobOpportunityService jobService;
    private ApplicationService applicationService;
    private ImportRowPersistenceService service;
    private ImportRowNormalizer normalizer;

    @BeforeEach
    void setUp() {
        companies = mock(CompanyRepository.class);
        jobs = mock(JobOpportunityRepository.class);
        jobService = mock(JobOpportunityService.class);
        applicationService = mock(ApplicationService.class);
        ApplicationUrlNormalizer urls = new ApplicationUrlNormalizer();
        service = new ImportRowPersistenceService(
            companies, jobs, jobService, applicationService, urls
        );
        normalizer = new ImportRowNormalizer(new ImportHeaderMapper(), urls);
        when(jobs.findAll()).thenReturn(List.of());
        JobOpportunityResponse jobResponse = mock(JobOpportunityResponse.class);
        when(jobResponse.id()).thenReturn(20L);
        when(jobService.create(any())).thenReturn(jobResponse);
        ApplicationResponse applicationResponse = mock(ApplicationResponse.class);
        when(applicationResponse.id()).thenReturn(30L);
        when(applicationService.createFromImport(any())).thenReturn(applicationResponse);
    }

    @Test
    void createsCompanyJobAndSavedApplicationWithDistinctNotes() {
        when(companies.findFirstByNameIgnoreCase("Acme"))
            .thenReturn(Optional.empty());
        when(companies.saveAndFlush(any())).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            ReflectionTestUtils.setField(company, "id", 10L);
            return company;
        });

        ImportRowPersistenceResult result = service.persist(row(Map.of(
            "position_title", "Engineer",
            "company_name", "Acme",
            "job_notes", "job note",
            "application_notes", "application note",
            "resume_version", "Resume A"
        )));

        assertThat(result.status()).isEqualTo(ImportRowOutcomeStatus.CREATED);
        assertThat(result.companyId()).isEqualTo(10L);
        assertThat(result.jobOpportunityId()).isEqualTo(20L);
        assertThat(result.applicationId()).isEqualTo(30L);
        ArgumentCaptor<JobOpportunityRequest> jobCaptor =
            ArgumentCaptor.forClass(JobOpportunityRequest.class);
        verify(jobService).create(jobCaptor.capture());
        assertThat(jobCaptor.getValue().notes()).isEqualTo("job note");
        ArgumentCaptor<ApplicationRequest> appCaptor =
            ArgumentCaptor.forClass(ApplicationRequest.class);
        verify(applicationService).createFromImport(appCaptor.capture());
        assertThat(appCaptor.getValue().status())
            .isEqualTo(ApplicationStatus.SAVED);
        assertThat(appCaptor.getValue().notes()).isEqualTo("application note");
        assertThat(appCaptor.getValue().resumeVersion()).isEqualTo("Resume A");
    }

    @Test
    void reusesCompanyCaseInsensitivelyAndAllowsBlankCompany() {
        Company existing = company("Acme", 11L);
        when(companies.findFirstByNameIgnoreCase("ACME"))
            .thenReturn(Optional.of(existing));

        assertThat(service.persist(row(Map.of(
            "position_title", "One", "company_name", "ACME"
        ))).companyId()).isEqualTo(11L);
        assertThat(service.persist(row(Map.of(
            "position_title", "Two"
        ))).companyId()).isNull();
    }

    @Test
    void importsCompanyTitleWarningsAndReportsThem() {
        ImportRowResult base = row(Map.of("position_title", "Engineer"));
        ImportRowResult warning = new ImportRowResult(
            base.rowNumber(), base.values(), base.errors(),
            List.of(new ImportIssue("position_title", "Possible duplicate")),
            null, null, List.of(), ImportProposedAction.REVIEW_WARNING, true
        );
        ImportRowPersistenceResult result = service.persist(warning);
        assertThat(result.status())
            .isEqualTo(ImportRowOutcomeStatus.CREATED_WITH_WARNING);
        assertThat(result.warnings()).containsExactly("Possible duplicate");
    }

    @Test
    void rechecksNormalizedUrlAndPreservesQueryDistinctions() {
        JobOpportunity duplicate = job("https://example.com/job?a=1#apply", 90L);
        when(jobs.findAll()).thenReturn(List.of(duplicate));

        ImportRowPersistenceResult skipped = service.persist(row(Map.of(
            "position_title", "Duplicate",
            "application_url", "HTTPS://EXAMPLE.COM:443/job?a=1"
        )));
        assertThat(skipped.status())
            .isEqualTo(ImportRowOutcomeStatus.SKIPPED_DUPLICATE);
        assertThat(skipped.duplicateJobOpportunityId()).isEqualTo(90L);

        ImportRowPersistenceResult distinct = service.persist(row(Map.of(
            "position_title", "Distinct",
            "application_url", "https://example.com/job?a=2"
        )));
        assertThat(distinct.status()).isEqualTo(ImportRowOutcomeStatus.CREATED);
    }

    @Test
    void surfacesCompanyUniquenessRaceForCleanTransactionRetry() {
        when(companies.findFirstByNameIgnoreCase("Acme"))
            .thenReturn(Optional.empty());
        when(companies.saveAndFlush(any()))
            .thenThrow(new DataIntegrityViolationException("unique"));
        assertThatThrownBy(() -> service.persist(row(Map.of(
            "position_title", "Engineer", "company_name", "Acme"
        )))).isInstanceOf(CompanyResolutionRaceException.class);
    }

    @Test
    void applicationFailureEscapesTheAtomicRowTransaction() {
        when(applicationService.createFromImport(any()))
            .thenThrow(new RuntimeException("application failed"));
        assertThatThrownBy(() -> service.persist(row(Map.of(
            "position_title", "Engineer"
        )))).hasMessage("application failed");
        verify(jobService).create(any());
    }

    @Test
    void declaresOneNewTransactionPerRow() throws Exception {
        Transactional annotation = ImportRowPersistenceService.class
            .getMethod("persist", ImportRowResult.class)
            .getAnnotation(Transactional.class);
        assertThat(annotation.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }

    private ImportRowResult row(Map<String, String> fields) {
        return normalizer.normalize(2, fields);
    }

    private Company company(String name, Long id) {
        Company company = new Company(
            name, null, null, null, null, null, null, null, null,
            null, null, false
        );
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    private JobOpportunity job(String url, Long id) {
        JobOpportunity job = new JobOpportunity(
            null, "Existing", null, null,
            com.chengukargbo.careeros.jobs.RemoteType.UNKNOWN, null,
            null, null, "USD", null, url, null, null, null,
            (short) 3, null, null, null
        );
        ReflectionTestUtils.setField(job, "id", id);
        return job;
    }
}
