import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import ApplicationsPage from "./ApplicationsPage";
import {
  getApplicationTracker,
  getApplications,
  persistCsvImport,
  previewCsvImport,
} from "../api/applicationsApi";
import { getJobs } from "../../jobs/api/jobsApi";
import type { Application } from "../types/application";
import type { ApplicationTrackerRow } from "../types/applicationTracker";
import type { JobOpportunity } from "../../jobs/types/job";

vi.mock("../api/applicationsApi", () => ({
  createApplication: vi.fn(),
  deleteApplication: vi.fn(),
  getApplicationTracker: vi.fn(),
  getApplications: vi.fn(),
  persistCsvImport: vi.fn(),
  previewCsvImport: vi.fn(),
  updateApplication: vi.fn(),
}));

vi.mock("../../jobs/api/jobsApi", () => ({
  getJobs: vi.fn(),
}));

const job: JobOpportunity = {
  id: 1,
  companyId: 1,
  companyName: "GitHub",
  positionTitle: "Platform Engineer",
  department: "Engineering",
  location: "New York, NY",
  remoteType: "HYBRID",
  employmentType: "Full-time",
  salaryMin: 100000,
  salaryMax: 130000,
  salaryCurrency: "USD",
  salaryNotes: null,
  applicationUrl: "https://example.com/jobs/1",
  source: "Company site",
  datePosted: "2026-08-01",
  closingDate: null,
  priority: 1,
  matchScore: 9,
  jobDescription: null,
  notes: "Job notes",
  createdAt: "2026-08-01T10:00:00Z",
  updatedAt: "2026-08-01T10:00:00Z",
};

const jobWithoutApplication: JobOpportunity = {
  ...job,
  id: 2,
  companyId: null,
  companyName: null,
  positionTitle: "Operations Engineer",
};

const application: Application = {
  id: 101,
  jobOpportunityId: 1,
  positionTitle: "Platform Engineer",
  companyId: 1,
  companyName: "GitHub",
  status: "APPLIED",
  resumeVersion: "Software Engineering",
  coverLetterNeeded: true,
  portfolioLink: null,
  githubLink: null,
  projectsToHighlight: null,
  skillsToEmphasize: null,
  interviewTopics: null,
  recruiterName: null,
  recruiterEmail: null,
  applicationDate: "2026-08-07",
  followUpDate: null,
  phoneScreenAt: null,
  interviewOneAt: null,
  interviewTwoAt: null,
  offerAt: null,
  rejectedAt: null,
  notes: "Application notes",
  createdAt: "2026-08-07T10:00:00Z",
  updatedAt: "2026-08-07T10:00:00Z",
};

function trackerRow(
  sourceJob: JobOpportunity,
  sourceApplication: Application | null,
): ApplicationTrackerRow {
  return {
    jobOpportunityId: sourceJob.id,
    companyId: sourceJob.companyId,
    companyName: sourceJob.companyName,
    positionTitle: sourceJob.positionTitle,
    department: sourceJob.department,
    location: sourceJob.location,
    remoteType: sourceJob.remoteType,
    employmentType: sourceJob.employmentType,
    salaryMin: sourceJob.salaryMin,
    salaryMax: sourceJob.salaryMax,
    salaryCurrency: sourceJob.salaryCurrency,
    salaryNotes: sourceJob.salaryNotes,
    applicationUrl: sourceJob.applicationUrl,
    source: sourceJob.source,
    datePosted: sourceJob.datePosted,
    closingDate: sourceJob.closingDate,
    priority: sourceJob.priority,
    matchScore: sourceJob.matchScore,
    jobDescription: sourceJob.jobDescription,
    jobNotes: sourceJob.notes,
    jobCreatedAt: sourceJob.createdAt,
    jobUpdatedAt: sourceJob.updatedAt,
    applicationId: sourceApplication?.id ?? null,
    status: sourceApplication?.status ?? null,
    resumeVersion: sourceApplication?.resumeVersion ?? null,
    coverLetterNeeded:
      sourceApplication?.coverLetterNeeded ?? null,
    portfolioLink: sourceApplication?.portfolioLink ?? null,
    githubLink: sourceApplication?.githubLink ?? null,
    projectsToHighlight:
      sourceApplication?.projectsToHighlight ?? null,
    skillsToEmphasize:
      sourceApplication?.skillsToEmphasize ?? null,
    interviewTopics: sourceApplication?.interviewTopics ?? null,
    recruiterName: sourceApplication?.recruiterName ?? null,
    recruiterEmail: sourceApplication?.recruiterEmail ?? null,
    applicationDate: sourceApplication?.applicationDate ?? null,
    followUpDate: sourceApplication?.followUpDate ?? null,
    phoneScreenAt: sourceApplication?.phoneScreenAt ?? null,
    interviewOneAt: sourceApplication?.interviewOneAt ?? null,
    interviewTwoAt: sourceApplication?.interviewTwoAt ?? null,
    offerAt: sourceApplication?.offerAt ?? null,
    rejectedAt: sourceApplication?.rejectedAt ?? null,
    applicationNotes: sourceApplication?.notes ?? null,
    applicationCreatedAt: sourceApplication?.createdAt ?? null,
    applicationUpdatedAt: sourceApplication?.updatedAt ?? null,
  };
}

describe("ApplicationsPage", () => {
  beforeEach(() => {
    vi.mocked(getApplications).mockResolvedValue([application]);
    vi.mocked(getJobs).mockResolvedValue([
      job,
      jobWithoutApplication,
    ]);
    vi.mocked(getApplicationTracker).mockResolvedValue({
      content: [
        trackerRow(job, application),
        trackerRow(jobWithoutApplication, null),
      ],
      page: 0,
      size: 25,
      totalRows: 2,
      totalPages: 1,
    });
    vi.stubGlobal("scrollTo", vi.fn());
  });

  it("renders canonical rows fetched from the tracker endpoint", async () => {
    render(<ApplicationsPage />);

    expect(await screen.findByRole("table")).toBeInTheDocument();
    expect(
      screen.getByRole("rowheader", { name: "Platform Engineer" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("rowheader", { name: "Operations Engineer" }),
    ).toBeInTheDocument();
    expect(getApplicationTracker).toHaveBeenCalledWith(
      expect.objectContaining({
        sort: "priority",
        direction: "asc",
        page: 0,
        size: 25,
      }),
    );
  });

  it("opens and closes Import Jobs without resetting tracker criteria", async () => {
    const user = userEvent.setup();
    render(<ApplicationsPage />);
    await screen.findByRole("table");

    const search = screen.getByRole("searchbox");
    await user.type(search, "platform");
    await user.click(screen.getByRole("button", { name: "Import Jobs" }));
    expect(screen.getByRole("dialog", { name: "Import Jobs" }))
      .toBeInTheDocument();
    await user.click(screen.getByRole("button", {
      name: "Close import preview",
    }));

    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    expect(search).toHaveValue("platform");
  });

  it("refreshes the active tracker query after a completed import", async () => {
    vi.mocked(previewCsvImport).mockResolvedValue({
      filename: "jobs.csv",
      totalRows: 1,
      createCount: 1,
      reviewCount: 0,
      duplicateCount: 0,
      invalidCount: 0,
      hasFileErrors: false,
      hasFileWarnings: false,
      fileErrors: [],
      fileWarnings: [],
      rows: [{
        rowNumber: 2,
        values: {
          positionTitle: "Imported Engineer",
          companyName: "Acme",
          location: null,
          workArrangement: "UNKNOWN",
          salaryMin: null,
          salaryMax: null,
          salaryCurrency: "USD",
          applicationUrl: null,
          priority: 3,
          matchScore: null,
          status: null,
        },
        errors: [],
        warnings: [],
        normalizedApplicationUrl: null,
        exactUrlDuplicate: null,
        companyTitleDuplicateCandidates: [],
        proposedAction: "CREATE",
        selectable: true,
      }],
    });
    vi.mocked(persistCsvImport).mockResolvedValue({
      batchId: 8,
      filename: "jobs.csv",
      totalRows: 1,
      selectedRows: 1,
      created: 1,
      createdWithWarnings: 0,
      skippedDuplicates: 0,
      failed: 0,
      rows: [],
    });
    const user = userEvent.setup();
    render(<ApplicationsPage />);
    await screen.findByRole("table");
    const search = screen.getByRole("searchbox");
    await user.type(search, "platform");
    await waitFor(() => expect(getApplicationTracker).toHaveBeenLastCalledWith(
      expect.objectContaining({ search: "platform" }),
    ));

    await user.click(screen.getByRole("button", { name: "Import Jobs" }));
    await user.upload(
      screen.getByLabelText("CSV file"),
      new File(["Job Title\nImported Engineer"], "jobs.csv", {
        type: "text/csv",
      }),
    );
    await user.click(screen.getByRole("button", { name: "Preview CSV" }));
    await screen.findByText("1 rows found");
    await user.click(screen.getByRole("button", { name: "Import Selected (1)" }));
    await screen.findByText("Import complete");

    expect(search).toHaveValue("platform");
    expect(getApplicationTracker).toHaveBeenLastCalledWith(
      expect.objectContaining({
        search: "platform",
        sort: "priority",
        direction: "asc",
      }),
    );
  });

  it("combines debounced search, filters, and server sorting", async () => {
    const user = userEvent.setup();
    render(<ApplicationsPage />);

    expect(await screen.findByLabelText("Search"))
      .toBeInTheDocument();
    expect(screen.getByLabelText("Status filter")).toBeInTheDocument();
    expect(screen.getByLabelText("Priority filter")).toBeInTheDocument();
    expect(screen.getByLabelText("Work Arrangement filter"))
      .toBeInTheDocument();
    expect(screen.getByLabelText("Company filter")).toBeInTheDocument();

    await user.type(screen.getByLabelText("Search"), "platform");
    await user.selectOptions(
      screen.getByLabelText("Status filter"),
      "APPLIED",
    );
    await user.selectOptions(
      screen.getByLabelText("Priority filter"),
      "1",
    );
    await user.selectOptions(
      screen.getByLabelText("Work Arrangement filter"),
      "HYBRID",
    );
    await user.selectOptions(
      screen.getByLabelText("Company filter"),
      "1",
    );
    await user.selectOptions(
      screen.getByLabelText("Sort by"),
      "positionTitle",
    );
    await user.selectOptions(
      screen.getByLabelText("Direction"),
      "desc",
    );

    await waitFor(() => {
      expect(getApplicationTracker).toHaveBeenLastCalledWith(
        expect.objectContaining({
          search: "platform",
          statuses: ["APPLIED"],
          priorities: [1],
          remoteTypes: ["HYBRID"],
          companyId: 1,
          sort: "positionTitle",
          direction: "desc",
          page: 0,
        }),
      );
    });
  });

  it("uses server pagination and resets the page for new criteria", async () => {
    vi.mocked(getApplicationTracker).mockImplementation(
      async (query = {}) => ({
        content: [trackerRow(job, application)],
        page: query.page ?? 0,
        size: 25,
        totalRows: 60,
        totalPages: 3,
      }),
    );

    const user = userEvent.setup();
    render(<ApplicationsPage />);

    expect(await screen.findByText("Page 1 of 3"))
      .toBeInTheDocument();
    expect(screen.getByText("60 results")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Next" }));

    await waitFor(() => {
      expect(getApplicationTracker).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 1 }),
      );
      expect(screen.getByText("Page 2 of 3")).toBeInTheDocument();
    });

    await user.selectOptions(
      screen.getByLabelText("Priority filter"),
      "2",
    );

    await waitFor(() => {
      expect(getApplicationTracker).toHaveBeenLastCalledWith(
        expect.objectContaining({ priorities: [2], page: 0 }),
      );
    });
  });

  it("preserves the server-returned row order", async () => {
    vi.mocked(getApplicationTracker).mockResolvedValue({
      content: [
        trackerRow(jobWithoutApplication, null),
        trackerRow(job, application),
      ],
      page: 0,
      size: 25,
      totalRows: 2,
      totalPages: 1,
    });

    render(<ApplicationsPage />);

    await screen.findByRole("table");
    expect(
      screen.getAllByRole("rowheader").map((cell) => cell.textContent),
    ).toEqual(["Operations Engineer", "Platform Engineer"]);
  });

  it("distinguishes global and filtered empty states and clears filters", async () => {
    vi.mocked(getApplicationTracker).mockResolvedValue({
      content: [],
      page: 0,
      size: 25,
      totalRows: 0,
      totalPages: 0,
    });

    const user = userEvent.setup();
    render(<ApplicationsPage />);

    expect(await screen.findByText("No tracked jobs found."))
      .toBeInTheDocument();

    await user.selectOptions(
      screen.getByLabelText("Status filter"),
      "OFFER",
    );

    expect(
      await screen.findByText("No records match the current filters."),
    ).toBeInTheDocument();

    const clearButtons = screen.getAllByRole("button", {
      name: "Clear filters",
    });
    await user.click(clearButtons.at(-1)!);

    await waitFor(() => {
      expect(screen.getByLabelText("Status filter")).toHaveValue("");
      expect(getApplicationTracker).toHaveBeenLastCalledWith(
        expect.objectContaining({
          statuses: undefined,
          page: 0,
          sort: "priority",
          direction: "asc",
        }),
      );
    });
  });

  it("preselects the correct job for a job-only row", async () => {
    const user = userEvent.setup();
    render(<ApplicationsPage />);

    await user.click(
      await screen.findByRole("button", {
        name: "Add application for Operations Engineer",
      }),
    );

    await waitFor(() => {
      expect(screen.getByLabelText("Job opportunity"))
        .toHaveValue("2");
    });
  });

  it("opens the existing application in the edit form", async () => {
    const user = userEvent.setup();
    render(<ApplicationsPage />);

    await user.click(
      await screen.findByRole("button", {
        name: "Edit application for Platform Engineer",
      }),
    );

    expect(
      screen.getByRole("heading", {
        name: "Edit Platform Engineer",
      }),
    ).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByLabelText("Job opportunity"))
        .toHaveValue("1");
    });
  });
});
