package com.chengukargbo.careeros.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.applications.tracker.ApplicationTrackerQueryEngine;
import com.chengukargbo.careeros.companies.Company;
import com.chengukargbo.careeros.jobs.JobOpportunity;
import com.chengukargbo.careeros.jobs.JobOpportunityRepository;
import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.automation.ApplicationAutomationService;
import com.chengukargbo.careeros.automation.AutomationEnums.State;
import com.chengukargbo.careeros.applications.history.ApplicationStatusHistoryService;
import com.chengukargbo.careeros.applications.lock.*;

@ExtendWith(MockitoExtension.class)
class ApplicationTrackerServiceTest {

    @Mock
    private JobOpportunityRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationTrackerQueryEngine queryEngine;

    @Mock
    private ApplicationAutomationService automationService;

    @Mock
    private ApplicationStatusHistoryService statusHistoryService;

    @Mock
    private ApplicationLockGuard lockGuard;

    @InjectMocks
    private ApplicationTrackerService trackerService;

    @Test
    void returnsOneFullyMappedRowPerJobIncludingOptionalRelationships() {
        Company company = company(10L, "GitHub");

        JobOpportunity withCompanyAndApplication = job(
            1L,
            company,
            "Platform Engineer",
            (short) 1
        );
        JobOpportunity withCompanyWithoutApplication = job(
            2L,
            company,
            "Support Engineer",
            (short) 2
        );
        JobOpportunity withoutCompanyWithApplication = job(
            3L,
            null,
            "Backend Engineer",
            (short) 3
        );
        JobOpportunity withoutCompanyOrApplication = job(
            4L,
            null,
            "Systems Engineer",
            (short) 4
        );

        Application firstApplication = application(
            101L,
            withCompanyAndApplication,
            "application notes"
        );
        Application secondApplication = application(
            103L,
            withoutCompanyWithApplication,
            "unlinked company application"
        );

        when(applicationRepository.findAll())
            .thenReturn(List.of(firstApplication, secondApplication));
        when(automationService.findExistingState(101L)).thenReturn(State.APPROVED_FOR_PREP);
        when(lockGuard.state(101L)).thenReturn(ApplicationLockState.NOT_SUBMITTED);
        when(automationService.findExistingState(103L)).thenReturn(State.NEEDS_ANSWERS);
        when(
            jobRepository
                .findAllByOrderByPriorityAscCreatedAtDescIdDesc()
        )
            .thenReturn(List.of(
                withCompanyAndApplication,
                withCompanyWithoutApplication,
                withoutCompanyWithApplication,
                withoutCompanyOrApplication
            ));

        List<ApplicationTrackerResponse> rows =
            trackerService.findAll();

        assertThat(rows)
            .hasSize(4)
            .extracting(ApplicationTrackerResponse::jobOpportunityId)
            .containsExactly(1L, 2L, 3L, 4L);

        ApplicationTrackerResponse complete = rows.get(0);

        assertThat(complete.jobOpportunityId()).isEqualTo(1L);
        assertThat(complete.companyId()).isEqualTo(10L);
        assertThat(complete.companyName()).isEqualTo("GitHub");
        assertThat(complete.positionTitle()).isEqualTo("Platform Engineer");
        assertThat(complete.department()).isEqualTo("Engineering");
        assertThat(complete.location()).isEqualTo("New York, NY");
        assertThat(complete.remoteType()).isEqualTo(RemoteType.HYBRID);
        assertThat(complete.employmentType()).isEqualTo("Full-time");
        assertThat(complete.salaryMin()).isEqualByComparingTo("100000.00");
        assertThat(complete.salaryMax()).isEqualByComparingTo("130000.00");
        assertThat(complete.salaryCurrency()).isEqualTo("USD");
        assertThat(complete.salaryNotes()).isEqualTo("Bonus eligible");
        assertThat(complete.applicationUrl())
            .isEqualTo("https://example.com/jobs/1");
        assertThat(complete.source()).isEqualTo("Company site");
        assertThat(complete.datePosted())
            .isEqualTo(LocalDate.parse("2026-08-01"));
        assertThat(complete.closingDate())
            .isEqualTo(LocalDate.parse("2026-08-31"));
        assertThat(complete.priority()).isEqualTo((short) 1);
        assertThat(complete.matchScore()).isEqualByComparingTo("8.5");
        assertThat(complete.jobDescription()).isEqualTo("Build the platform");
        assertThat(complete.jobNotes()).isEqualTo("job notes");
        assertThat(complete.jobCreatedAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-01T10:00:00Z"));
        assertThat(complete.jobUpdatedAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-02T10:00:00Z"));

        assertThat(complete.applicationId()).isEqualTo(101L);
        assertThat(complete.status()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(complete.resumeVersion()).isEqualTo("Platform resume");
        assertThat(complete.coverLetterNeeded()).isTrue();
        assertThat(complete.portfolioLink()).isEqualTo("https://portfolio.test");
        assertThat(complete.githubLink()).isEqualTo("https://github.com/test");
        assertThat(complete.projectsToHighlight()).isEqualTo("CareerOS");
        assertThat(complete.skillsToEmphasize()).isEqualTo("Java, React");
        assertThat(complete.interviewTopics()).isEqualTo("System design");
        assertThat(complete.recruiterName()).isEqualTo("Alex Recruiter");
        assertThat(complete.recruiterEmail()).isEqualTo("alex@example.com");
        assertThat(complete.applicationDate())
            .isEqualTo(LocalDate.parse("2026-08-03"));
        assertThat(complete.followUpDate())
            .isEqualTo(LocalDate.parse("2026-08-10"));
        assertThat(complete.phoneScreenAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-11T14:00:00Z"));
        assertThat(complete.interviewOneAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-12T14:00:00Z"));
        assertThat(complete.interviewTwoAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-13T14:00:00Z"));
        assertThat(complete.offerAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-14T14:00:00Z"));
        assertThat(complete.rejectedAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-15T14:00:00Z"));
        assertThat(complete.applicationNotes()).isEqualTo("application notes");
        assertThat(complete.applicationCreatedAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-03T10:00:00Z"));
        assertThat(complete.applicationUpdatedAt())
            .isEqualTo(OffsetDateTime.parse("2026-08-04T10:00:00Z"));
        assertThat(complete.automationState()).isEqualTo(State.APPROVED_FOR_PREP);
        assertThat(complete.lockState()).isEqualTo(ApplicationLockState.NOT_SUBMITTED);
        assertThat(complete.statusDate())
            .isEqualTo(OffsetDateTime.parse("2026-08-03T00:00:00Z"));

        assertThat(rows.get(1).companyName()).isEqualTo("GitHub");
        assertThat(rows.get(1).applicationId()).isNull();
        assertThat(rows.get(1).status()).isNull();
        assertThat(rows.get(1).coverLetterNeeded()).isNull();
        assertThat(rows.get(1).applicationNotes()).isNull();
        assertThat(rows.get(1).applicationCreatedAt()).isNull();
        assertThat(rows.get(2).companyId()).isNull();
        assertThat(rows.get(2).applicationId()).isEqualTo(103L);
        assertThat(rows.get(3).companyId()).isNull();
        assertThat(rows.get(3).applicationId()).isNull();

        verify(jobRepository)
            .findAllByOrderByPriorityAscCreatedAtDescIdDesc();
    }

    @Test
    void resolvesOneCanonicalRowWithoutDependingOnTrackerPagination() {
        JobOpportunity job = job(
            77L, null, "Focused Engineer", (short) 2
        );
        Application application = application(177L, job, "Focused notes");
        ReflectionTestUtils.setField(application, "status", ApplicationStatus.PREPARING);
        OffsetDateTime historyDate = OffsetDateTime.parse("2026-08-09T15:00:00Z");
        when(jobRepository.findById(77L)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobOpportunityId(77L))
            .thenReturn(Optional.of(application));
        when(automationService.findExistingState(177L)).thenReturn(State.NOT_APPROVED);
        when(statusHistoryService.latestEventForStatus(177L, ApplicationStatus.PREPARING))
            .thenReturn(historyDate);

        ApplicationTrackerResponse row =
            trackerService.findByJobOpportunityId(77L);

        assertThat(row.jobOpportunityId()).isEqualTo(77L);
        assertThat(row.applicationId()).isEqualTo(177L);
        assertThat(row.applicationNotes()).isEqualTo("Focused notes");
        assertThat(row.statusDate()).isEqualTo(historyDate);
        verify(jobRepository).findById(77L);
        verify(applicationRepository).findByJobOpportunityId(77L);
    }

    private Company company(Long id, String name) {
        Company company = new Company(
            name,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false
        );
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    private JobOpportunity job(
        Long id,
        Company company,
        String title,
        short priority
    ) {
        JobOpportunity job = new JobOpportunity(
            company,
            title,
            "Engineering",
            "New York, NY",
            RemoteType.HYBRID,
            "Full-time",
            new BigDecimal("100000.00"),
            new BigDecimal("130000.00"),
            "USD",
            "Bonus eligible",
            "https://example.com/jobs/1",
            "Company site",
            LocalDate.parse("2026-08-01"),
            LocalDate.parse("2026-08-31"),
            priority,
            new BigDecimal("8.5"),
            "Build the platform",
            "job notes"
        );
        ReflectionTestUtils.setField(job, "id", id);
        ReflectionTestUtils.setField(
            job,
            "createdAt",
            OffsetDateTime.parse("2026-08-01T10:00:00Z")
        );
        ReflectionTestUtils.setField(
            job,
            "updatedAt",
            OffsetDateTime.parse("2026-08-02T10:00:00Z")
        );
        return job;
    }

    private Application application(
        Long id,
        JobOpportunity job,
        String notes
    ) {
        Application application = new Application(
            job,
            ApplicationStatus.APPLIED,
            "Platform resume",
            true,
            "https://portfolio.test",
            "https://github.com/test",
            "CareerOS",
            "Java, React",
            "System design",
            "Alex Recruiter",
            "alex@example.com",
            LocalDate.parse("2026-08-03"),
            LocalDate.parse("2026-08-10"),
            OffsetDateTime.parse("2026-08-11T14:00:00Z"),
            OffsetDateTime.parse("2026-08-12T14:00:00Z"),
            OffsetDateTime.parse("2026-08-13T14:00:00Z"),
            OffsetDateTime.parse("2026-08-14T14:00:00Z"),
            OffsetDateTime.parse("2026-08-15T14:00:00Z"),
            notes
        );
        ReflectionTestUtils.setField(application, "id", id);
        ReflectionTestUtils.setField(
            application,
            "createdAt",
            OffsetDateTime.parse("2026-08-03T10:00:00Z")
        );
        ReflectionTestUtils.setField(
            application,
            "updatedAt",
            OffsetDateTime.parse("2026-08-04T10:00:00Z")
        );
        return application;
    }
}
