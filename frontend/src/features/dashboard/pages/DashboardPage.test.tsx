import { render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import DashboardPage from "./DashboardPage";
import { getApplications } from "../../applications/api/applicationsApi";
import { getJobs } from "../../jobs/api/jobsApi";
import { getCompanies } from "../../companies/api/companiesApi";

vi.mock("../../applications/api/applicationsApi", () => ({
  getApplications: vi.fn(),
}));

vi.mock("../../jobs/api/jobsApi", () => ({
  getJobs: vi.fn(),
}));

vi.mock("../../companies/api/companiesApi", () => ({
  getCompanies: vi.fn(),
}));

const mockedGetApplications = vi.mocked(getApplications);
const mockedGetJobs = vi.mocked(getJobs);
const mockedGetCompanies = vi.mocked(getCompanies);

describe("DashboardPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    vi.useFakeTimers({
        shouldAdvanceTime: true,
    });

    vi.setSystemTime(
        new Date("2026-08-08T12:00:00-04:00")
    );
  });
    afterEach(() => {
        vi.useRealTimers();
    });

  it("displays dashboard metrics and pipeline counts", async () => {
    mockedGetApplications.mockResolvedValue([
      {
        id: 1,
        jobOpportunityId: 1,
        positionTitle: "Software Engineer",
        companyId: 1,
        companyName: "GitHub",
        status: "PHONE_SCREEN",
        resumeVersion: "Software Engineering",
        coverLetterNeeded: false,
        portfolioLink: null,
        githubLink: null,
        projectsToHighlight: null,
        skillsToEmphasize: null,
        interviewTopics: null,
        recruiterName: null,
        recruiterEmail: null,
        applicationDate: "2026-08-01",
        followUpDate: "2026-08-08",
        phoneScreenAt: "2026-08-12T14:00:00Z",
        interviewOneAt: null,
        interviewTwoAt: null,
        offerAt: null,
        rejectedAt: null,
        notes: null,
        createdAt: "2026-08-01T12:00:00Z",
        updatedAt: "2026-08-01T12:00:00Z",
      },
      {
        id: 2,
        jobOpportunityId: 2,
        positionTitle: "Support Engineer",
        companyId: 2,
        companyName: "PostHog",
        status: "OFFER",
        resumeVersion: "Technical Support",
        coverLetterNeeded: false,
        portfolioLink: null,
        githubLink: null,
        projectsToHighlight: null,
        skillsToEmphasize: null,
        interviewTopics: null,
        recruiterName: null,
        recruiterEmail: null,
        applicationDate: "2026-08-02",
        followUpDate: "2026-08-07",
        phoneScreenAt: null,
        interviewOneAt: null,
        interviewTwoAt: null,
        offerAt: "2026-08-07T18:00:00Z",
        rejectedAt: null,
        notes: null,
        createdAt: "2026-08-02T12:00:00Z",
        updatedAt: "2026-08-07T18:00:00Z",
      },
    ]);

    mockedGetJobs.mockResolvedValue([
      {
        id: 1,
        companyId: 1,
        companyName: "GitHub",
        positionTitle: "Software Engineer",
        department: null,
        location: "Remote",
        remoteType: "REMOTE",
        employmentType: "Full-time",
        salaryMin: null,
        salaryMax: null,
        salaryCurrency: "USD",
        salaryNotes: null,
        applicationUrl: null,
        source: null,
        datePosted: null,
        closingDate: null,
        priority: 1,
        matchScore: 9,
        jobDescription: null,
        notes: null,
        createdAt: "2026-08-01T12:00:00Z",
        updatedAt: "2026-08-01T12:00:00Z",
      },
      {
        id: 2,
        companyId: 2,
        companyName: "PostHog",
        positionTitle: "Support Engineer",
        department: null,
        location: "Remote",
        remoteType: "REMOTE",
        employmentType: "Full-time",
        salaryMin: null,
        salaryMax: null,
        salaryCurrency: "USD",
        salaryNotes: null,
        applicationUrl: null,
        source: null,
        datePosted: null,
        closingDate: null,
        priority: 3,
        matchScore: 8,
        jobDescription: null,
        notes: null,
        createdAt: "2026-08-02T12:00:00Z",
        updatedAt: "2026-08-02T12:00:00Z",
      },
    ]);

    mockedGetCompanies.mockResolvedValue([
      {
        id: 1,
        name: "GitHub",
        websiteUrl: null,
        careersUrl: null,
        industry: "Software",
        companyType: "SaaS",
        mission: null,
        products: null,
        techStack: null,
        remotePolicy: null,
        salaryNotes: null,
        generalNotes: null,
        dreamCompany: true,
        createdAt: "2026-08-01T12:00:00Z",
        updatedAt: "2026-08-01T12:00:00Z",
      },
      {
        id: 2,
        name: "PostHog",
        websiteUrl: null,
        careersUrl: null,
        industry: "Software",
        companyType: "SaaS",
        mission: null,
        products: null,
        techStack: null,
        remotePolicy: null,
        salaryNotes: null,
        generalNotes: null,
        dreamCompany: false,
        createdAt: "2026-08-02T12:00:00Z",
        updatedAt: "2026-08-02T12:00:00Z",
      },
    ]);

    render(<DashboardPage />);

    expect(
      await screen.findByRole("heading", { name: "Dashboard" }),
    ).toBeInTheDocument();

    expect(screen.getByText("Applications")).toBeInTheDocument();
    expect(screen.getByText("Active interviews")).toBeInTheDocument();
    expect(screen.getByText("Offers")).toBeInTheDocument();
    expect(screen.getByText("High-priority jobs")).toBeInTheDocument();
    expect(screen.getByText("Dream companies")).toBeInTheDocument();
    expect(screen.getByText("Follow-ups due")).toBeInTheDocument();

    expect(
      screen.getByRole("heading", { name: "Hiring Pipeline" }),
    ).toBeInTheDocument();

    expect(screen.getByText("Phone Screen", {
        selector: "p",
    }),
    ).toBeInTheDocument();

    expect(screen.getByText("Offer")).toBeInTheDocument();
  });

  it("shows due follow-ups and excludes terminal statuses", async () => {
    mockedGetApplications.mockResolvedValue([
      {
        id: 1,
        jobOpportunityId: 1,
        positionTitle: "Software Engineer",
        companyId: 1,
        companyName: "GitHub",
        status: "APPLIED",
        resumeVersion: null,
        coverLetterNeeded: false,
        portfolioLink: null,
        githubLink: null,
        projectsToHighlight: null,
        skillsToEmphasize: null,
        interviewTopics: null,
        recruiterName: null,
        recruiterEmail: null,
        applicationDate: "2026-08-01",
        followUpDate: "2026-08-08",
        phoneScreenAt: null,
        interviewOneAt: null,
        interviewTwoAt: null,
        offerAt: null,
        rejectedAt: null,
        notes: null,
        createdAt: "2026-08-01T12:00:00Z",
        updatedAt: "2026-08-01T12:00:00Z",
      },
      {
        id: 2,
        jobOpportunityId: 2,
        positionTitle: "Closed Role",
        companyId: 2,
        companyName: "Example Co",
        status: "REJECTED",
        resumeVersion: null,
        coverLetterNeeded: false,
        portfolioLink: null,
        githubLink: null,
        projectsToHighlight: null,
        skillsToEmphasize: null,
        interviewTopics: null,
        recruiterName: null,
        recruiterEmail: null,
        applicationDate: "2026-08-01",
        followUpDate: "2026-08-01",
        phoneScreenAt: null,
        interviewOneAt: null,
        interviewTwoAt: null,
        offerAt: null,
        rejectedAt: "2026-08-05T12:00:00Z",
        notes: null,
        createdAt: "2026-08-01T12:00:00Z",
        updatedAt: "2026-08-05T12:00:00Z",
      },
    ]);

    mockedGetJobs.mockResolvedValue([]);
    mockedGetCompanies.mockResolvedValue([]);

    render(<DashboardPage />);

    expect(
      await screen.findByRole("heading", {
        name: "Needs Attention",
        }),
        ).toBeInTheDocument();

        expect(
        screen.getByRole("heading", {
            name: "Software Engineer",
        }),
        ).toBeInTheDocument();

        expect(
        screen.queryByRole("heading", {
            name: "Closed Role",
        }),
        ).not.toBeInTheDocument();
    });

    it("shows upcoming interviews", async () => {
        mockedGetApplications.mockResolvedValue([
        {
            id: 1,
            jobOpportunityId: 1,
            positionTitle: "Software Engineer",
            companyId: 1,
            companyName: "GitHub",
            status: "PHONE_SCREEN",
            resumeVersion: null,
            coverLetterNeeded: false,
            portfolioLink: null,
            githubLink: null,
            projectsToHighlight: null,
            skillsToEmphasize: null,
            interviewTopics: null,
            recruiterName: null,
            recruiterEmail: null,
            applicationDate: "2026-08-01",
            followUpDate: null,
            phoneScreenAt: "2026-08-12T14:00:00Z",
            interviewOneAt: null,
            interviewTwoAt: null,
            offerAt: null,
            rejectedAt: null,
            notes: null,
            createdAt: "2026-08-01T12:00:00Z",
            updatedAt: "2026-08-01T12:00:00Z",
        },
        ]);

        mockedGetJobs.mockResolvedValue([]);
        mockedGetCompanies.mockResolvedValue([]);

        render(<DashboardPage />);

        expect(
        await screen.findByRole("heading", {
            name: "Upcoming Interviews",
        }),
        ).toBeInTheDocument();

        expect(
        screen.getByRole("heading", {
            name: "Software Engineer",
        }),
        ).toBeInTheDocument();

        expect(
        screen.getByText(/Phone Screen:/),
        ).toBeInTheDocument();
    });

    it("shows today's prioritized actions", async () => {
    mockedGetApplications.mockResolvedValue([
        {
        id: 1,
        jobOpportunityId: 1,
        positionTitle: "Software Engineer",
        companyId: 1,
        companyName: "GitHub",
        status: "PHONE_SCREEN",
        resumeVersion: "Software Engineering",
        coverLetterNeeded: false,
        portfolioLink: null,
        githubLink: null,
        projectsToHighlight: null,
        skillsToEmphasize: null,
        interviewTopics: null,
        recruiterName: null,
        recruiterEmail: null,
        applicationDate: "2026-08-01",
        followUpDate: "2026-08-08",
        phoneScreenAt: "2026-08-12T14:00:00Z",
        interviewOneAt: null,
        interviewTwoAt: null,
        offerAt: null,
        rejectedAt: null,
        notes: null,
        createdAt: "2026-08-01T12:00:00Z",
        updatedAt: "2026-08-01T12:00:00Z",
        },
        {
        id: 2,
        jobOpportunityId: 2,
        positionTitle: "Support Engineer",
        companyId: 2,
        companyName: "PostHog",
        status: "PREPARING",
        resumeVersion: "Technical Support",
        coverLetterNeeded: true,
        portfolioLink: null,
        githubLink: null,
        projectsToHighlight: null,
        skillsToEmphasize: null,
        interviewTopics: null,
        recruiterName: null,
        recruiterEmail: null,
        applicationDate: null,
        followUpDate: null,
        phoneScreenAt: null,
        interviewOneAt: null,
        interviewTwoAt: null,
        offerAt: null,
        rejectedAt: null,
        notes: null,
        createdAt: "2026-08-02T12:00:00Z",
        updatedAt: "2026-08-02T12:00:00Z",
        },
    ]);

    mockedGetJobs.mockResolvedValue([
        {
        id: 1,
        companyId: 1,
        companyName: "GitHub",
        positionTitle: "Software Engineer",
        department: null,
        location: "Remote",
        remoteType: "REMOTE",
        employmentType: "Full-time",
        salaryMin: null,
        salaryMax: null,
        salaryCurrency: "USD",
        salaryNotes: null,
        applicationUrl: null,
        source: null,
        datePosted: null,
        closingDate: null,
        priority: 1,
        matchScore: 9,
        jobDescription: null,
        notes: null,
        createdAt: "2026-08-01T12:00:00Z",
        updatedAt: "2026-08-01T12:00:00Z",
        },
        {
        id: 2,
        companyId: 2,
        companyName: "PostHog",
        positionTitle: "Support Engineer",
        department: null,
        location: "Remote",
        remoteType: "REMOTE",
        employmentType: "Full-time",
        salaryMin: null,
        salaryMax: null,
        salaryCurrency: "USD",
        salaryNotes: null,
        applicationUrl: null,
        source: null,
        datePosted: null,
        closingDate: null,
        priority: 2,
        matchScore: 8.5,
        jobDescription: null,
        notes: null,
        createdAt: "2026-08-02T12:00:00Z",
        updatedAt: "2026-08-02T12:00:00Z",
        },
        {
        id: 3,
        companyId: 3,
        companyName: "Mozilla",
        positionTitle: "Frontend Developer",
        department: null,
        location: "Remote",
        remoteType: "REMOTE",
        employmentType: "Full-time",
        salaryMin: null,
        salaryMax: null,
        salaryCurrency: "USD",
        salaryNotes: null,
        applicationUrl: null,
        source: null,
        datePosted: null,
        closingDate: null,
        priority: 1,
        matchScore: 9.2,
        jobDescription: null,
        notes: null,
        createdAt: "2026-08-03T12:00:00Z",
        updatedAt: "2026-08-03T12:00:00Z",
        },
    ]);

    mockedGetCompanies.mockResolvedValue([]);

    render(<DashboardPage />);

    expect(
        await screen.findByRole("heading", {
        name: "Today's Actions",
        }),
    ).toBeInTheDocument();

    expect(
        screen.getByRole("heading", {
        name: "Follow up",
        }),
    ).toBeInTheDocument();

    expect(
        screen.getByRole("heading", {
        name: "Finish application",
        }),
    ).toBeInTheDocument();

    expect(
        screen.getByRole("heading", {
        name: "Prepare for Phone Screen",
        }),
    ).toBeInTheDocument();

    expect(
        screen.getByRole("heading", {
        name: "Apply to priority job",
        }),
    ).toBeInTheDocument();

    expect(
        screen.getByText("4 actions need attention."),
    ).toBeInTheDocument();
    });

  it("shows dashboard empty states", async () => {
    mockedGetApplications.mockResolvedValue([]);
    mockedGetJobs.mockResolvedValue([]);
    mockedGetCompanies.mockResolvedValue([]);

    render(<DashboardPage />);

    expect(
      await screen.findByText(
        "No application follow-ups are due.",
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByText(
        "No upcoming interviews are scheduled.",
      ),
    ).toBeInTheDocument();
  });

  it("shows an API error", async () => {
    mockedGetApplications.mockRejectedValue(
      new Error("Applications could not be loaded."),
    );

    mockedGetJobs.mockResolvedValue([]);
    mockedGetCompanies.mockResolvedValue([]);

    render(<DashboardPage />);

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Applications could not be loaded.",
    );
  });
});