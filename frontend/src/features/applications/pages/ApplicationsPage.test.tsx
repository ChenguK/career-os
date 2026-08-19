import {
  render as testingLibraryRender,
  screen,
  waitFor,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { MemoryRouter } from "react-router-dom";
import type { ReactElement } from "react";

import ApplicationsPage from "./ApplicationsPage";
import {
  getApplicationTracker,
  getApplicationTrackerRow,
  exportApplicationTrackerCsv,
  exportApplicationTrackerXlsx,
  getApplications,
  persistCsvImport,
  previewCsvImport,
  updateApplication,
} from "../api/applicationsApi";
import { getJobs } from "../../jobs/api/jobsApi";
import { updateJob } from "../../jobs/api/jobsApi";
import { getCompanies } from "../../companies/api/companiesApi";
import type { Application } from "../types/application";
import type { ApplicationTrackerRow } from "../types/applicationTracker";
import type { JobOpportunity } from "../../jobs/types/job";

vi.mock("../api/applicationsApi", () => ({
  createApplication: vi.fn(),
  deleteApplication: vi.fn(),
  exportApplicationTrackerCsv: vi.fn(),
  exportApplicationTrackerXlsx: vi.fn(),
  getApplicationTracker: vi.fn(),
  getApplicationTrackerRow: vi.fn(),
  getApplications: vi.fn(),
  getApplicationStatusHistory: vi.fn().mockResolvedValue([]),
  getApplicationAutomation: vi.fn().mockResolvedValue({
    id: 1, applicationId: 1, state: "NOT_APPROVED",
    submissionMode: "PREPARE_ONLY", atsType: "UNKNOWN",
    unresolvedRequiredCount: 0, needsReviewCount: 0, blockerCount: 0,
    blockReason: null, approvedForPrepAt: null, readyForReviewAt: null,
    approvedToSubmitAt: null, updatedAt: "2026-08-18T12:00:00Z",
  }),
  automationAction: vi.fn(),
  setApplicationAtsType: vi.fn(),
  getApplicationPreparation: vi.fn().mockResolvedValue({ capability: "SESSION_ONLY", session: null }),
  getApplicationPreparationEvents: vi.fn().mockResolvedValue([]),
  preparationAction: vi.fn(),
  getApplicationLock: vi.fn().mockResolvedValue({id:1,applicationId:101,lockState:"NOT_SUBMITTED",changedAt:"2026-08-18T12:00:00Z",reason:null,createdAt:"2026-08-18T12:00:00Z",updatedAt:"2026-08-18T12:00:00Z"}),
  getApplicationLockHistory: vi.fn().mockResolvedValue([]),
  applicationLockAction: vi.fn(),
  persistCsvImport: vi.fn(),
  previewCsvImport: vi.fn(),
  updateApplication: vi.fn(),
  markApplicationApplied: vi.fn(),
}));

vi.mock("../../jobs/api/jobsApi", () => ({
  getJobs: vi.fn(),
  updateJob: vi.fn(),
}));

vi.mock("../../companies/api/companiesApi", () => ({
  getCompanies: vi.fn(),
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

function render(ui: ReactElement) {
  return testingLibraryRender(
    <MemoryRouter initialEntries={["/applications"]}>
      {ui}
    </MemoryRouter>,
  );
}

function renderHandoff(state: unknown) {
  return testingLibraryRender(
    <MemoryRouter initialEntries={[{
      pathname: "/applications",
      state,
    }]}>
      <ApplicationsPage />
    </MemoryRouter>,
  );
}

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
    vi.clearAllMocks();
    vi.mocked(getApplications).mockResolvedValue([application]);
    vi.mocked(getJobs).mockResolvedValue([
      job,
      jobWithoutApplication,
    ]);
    vi.mocked(getCompanies).mockResolvedValue([{
      id: 1,
      name: "GitHub",
      websiteUrl: null,
      careersUrl: null,
      industry: null,
      companyType: null,
      mission: null,
      products: null,
      techStack: null,
      remotePolicy: null,
      salaryNotes: null,
      generalNotes: null,
      dreamCompany: false,
      createdAt: "2026-08-01T10:00:00Z",
      updatedAt: "2026-08-01T10:00:00Z",
    }]);
    vi.mocked(updateJob).mockResolvedValue(job);
    vi.mocked(updateApplication).mockResolvedValue(application);
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
    vi.mocked(getApplicationTrackerRow).mockResolvedValue(
      trackerRow(job, application),
    );
    vi.stubGlobal("scrollTo", vi.fn());
    vi.mocked(exportApplicationTrackerCsv).mockResolvedValue({
      blob: new Blob(["csv"]),
      filename: "applications.csv",
    });
    vi.mocked(exportApplicationTrackerXlsx).mockResolvedValue({
      blob: new Blob(["xlsx"]),
      filename: "applications.xlsx",
    });
    vi.stubGlobal("URL", {
      ...URL,
      createObjectURL: vi.fn(() => "blob:test"),
      revokeObjectURL: vi.fn(),
    });
    vi.spyOn(HTMLAnchorElement.prototype, "click")
      .mockImplementation(() => undefined);
  });

  it("exports current criteria without changing tracker controls", async () => {
    const user = userEvent.setup();
    render(<ApplicationsPage />);
    await screen.findByRole("table");
    await user.type(screen.getByRole("searchbox"), "platform");
    await user.selectOptions(screen.getByLabelText("Status filter"), "APPLIED");
    await user.selectOptions(screen.getByLabelText("Sort by"), "company");
    await user.selectOptions(screen.getByLabelText("Direction"), "desc");
    await waitFor(() => expect(getApplicationTracker).toHaveBeenLastCalledWith(
      expect.objectContaining({ search: "platform" }),
    ));

    await user.click(screen.getByRole("button", { name: "Current View (.csv)" }));

    expect(exportApplicationTrackerCsv).toHaveBeenCalledWith(
      "CURRENT_VIEW",
      expect.objectContaining({
        search: "platform", statuses: ["APPLIED"], sort: "company",
        direction: "desc",
      }),
    );
    expect(screen.getByRole("searchbox")).toHaveValue("platform");
    expect(screen.getByLabelText("Status filter")).toHaveValue("APPLIED");
    expect(HTMLAnchorElement.prototype.click).toHaveBeenCalled();
  });

  it("exports all without criteria and reports failures for retry", async () => {
    vi.mocked(exportApplicationTrackerCsv)
      .mockRejectedValueOnce(new Error("Export limit exceeded"))
      .mockResolvedValueOnce({ blob: new Blob(["csv"]), filename: "all.csv" });
    const user = userEvent.setup();
    render(<ApplicationsPage />);
    await screen.findByRole("table");

    const all = screen.getByRole("button", { name: "All Applications (.csv)" });
    await user.click(all);
    expect(await screen.findByRole("alert")).toHaveTextContent("Export limit exceeded");
    expect(exportApplicationTrackerCsv).toHaveBeenLastCalledWith("ALL", {});

    await user.click(all);
    await waitFor(() => expect(exportApplicationTrackerCsv).toHaveBeenCalledTimes(2));
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  it("offers XLSX current-view and all exports through the backend", async () => {
    const user = userEvent.setup();
    render(<ApplicationsPage />);
    await screen.findByRole("table");
    await user.type(screen.getByRole("searchbox"), "platform");
    await waitFor(() => expect(getApplicationTracker).toHaveBeenLastCalledWith(
      expect.objectContaining({ search: "platform" }),
    ));

    await user.click(screen.getByRole("button", { name: "Current View (.xlsx)" }));
    expect(exportApplicationTrackerXlsx).toHaveBeenCalledWith(
      "CURRENT_VIEW",
      expect.objectContaining({ search: "platform" }),
    );
    await user.click(screen.getByRole("button", { name: "All Applications (.xlsx)" }));
    expect(exportApplicationTrackerXlsx).toHaveBeenLastCalledWith("ALL", {});
    expect(screen.getByRole("searchbox")).toHaveValue("platform");
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
      screen.getByLabelText("CSV or XLSX file"),
      new File(["Job Title\nImported Engineer"], "jobs.csv", {
        type: "text/csv",
      }),
    );
    await user.click(screen.getByRole("button", { name: "Preview file" }));
    await screen.findByText("1 rows found");
    await user.click(screen.getByRole("button", { name: "Import Selected (1)" }));
    await screen.findByText("Import complete");

    expect(getApplications).toHaveBeenCalledTimes(2);
    expect(getJobs).toHaveBeenCalledTimes(2);
    expect(getCompanies).toHaveBeenCalledTimes(2);
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

  it("opens the full canonical record editor and populates both sections", async () => {
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
    expect(screen.getByLabelText("Position title"))
      .toHaveValue("Platform Engineer");
    expect(screen.getByLabelText("Location"))
      .toHaveValue("New York, NY");
    expect(screen.getByLabelText("Status")).toHaveValue("APPLIED");
    expect(screen.getByLabelText("Résumé version"))
      .toHaveValue("");
    expect(screen.getByText(/Legacy résumé label: Software Engineering/)).toBeInTheDocument();
  });

  it("keeps import and tracker editing mutually exclusive", async () => {
    const user = userEvent.setup();
    render(<ApplicationsPage />);
    await screen.findByRole("table");

    await user.click(screen.getByRole("button", { name: "Import Jobs" }));
    expect(screen.getByRole("dialog", { name: "Import Jobs" }))
      .toBeInTheDocument();
    await user.click(screen.getByRole("button", {
      name: "Edit application for Platform Engineer",
    }));
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Edit Platform Engineer" }))
      .toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Import Jobs" }));
    expect(screen.queryByRole("heading", { name: "Edit Platform Engineer" }))
      .not.toBeInTheDocument();
    expect(screen.getByRole("dialog", { name: "Import Jobs" }))
      .toBeInTheDocument();
  });

  it("saves job and application details through their independent IDs", async () => {
    const user = userEvent.setup();
    render(<ApplicationsPage />);
    await user.click(await screen.findByRole("button", {
      name: "Edit application for Platform Engineer",
    }));

    await user.click(screen.getByRole("button", { name: "Save Job Details" }));
    expect(updateJob).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ positionTitle: "Platform Engineer" }),
    );
    expect(updateApplication).not.toHaveBeenCalled();

    await user.click(screen.getByRole("button", {
      name: "Save Application Details",
    }));
    expect(updateApplication).toHaveBeenCalledWith(
      101,
      expect.objectContaining({ jobOpportunityId: 1, status: "APPLIED" }),
    );
    expect(updateJob).toHaveBeenCalledTimes(1);
  });

  it("cancels editing without saving or resetting tracker criteria", async () => {
    vi.mocked(getApplicationTracker).mockImplementation(async (query = {}) => ({
      content: [trackerRow(job, application)],
      page: query.page ?? 0,
      size: 25,
      totalRows: 60,
      totalPages: 3,
    }));
    const user = userEvent.setup();
    render(<ApplicationsPage />);
    await screen.findByRole("table");
    await user.type(screen.getByRole("searchbox"), "platform");
    await user.selectOptions(screen.getByLabelText("Status filter"), "APPLIED");
    await user.selectOptions(screen.getByLabelText("Sort by"), "company");
    await user.selectOptions(screen.getByLabelText("Direction"), "desc");
    await waitFor(() => expect(getApplicationTracker).toHaveBeenLastCalledWith(
      expect.objectContaining({
        search: "platform",
        statuses: ["APPLIED"],
        sort: "company",
        direction: "desc",
      }),
    ));
    await user.click(screen.getByRole("button", {
      name: "Edit application for Platform Engineer",
    }));
    await user.click(screen.getAllByRole("button", { name: "Cancel" })[1]);

    expect(screen.queryByRole("heading", { name: "Edit Platform Engineer" }))
      .not.toBeInTheDocument();
    expect(screen.getByRole("searchbox")).toHaveValue("platform");
    expect(screen.getByLabelText("Status filter")).toHaveValue("APPLIED");
    expect(screen.getByLabelText("Sort by")).toHaveValue("company");
    expect(screen.getByLabelText("Direction")).toHaveValue("desc");
    expect(updateJob).not.toHaveBeenCalled();
    expect(updateApplication).not.toHaveBeenCalled();
  });

  it("opens Add Application for an APPLY handoff to a job-only row off page 1", async () => {
    vi.mocked(getApplicationTracker).mockResolvedValue({
      content: [trackerRow(job, application)],
      page: 0,
      size: 25,
      totalRows: 26,
      totalPages: 2,
    });
    vi.mocked(getApplicationTrackerRow).mockResolvedValue(
      trackerRow(jobWithoutApplication, null),
    );

    renderHandoff({
      dashboardHandoff: {
        action: "APPLY",
        jobOpportunityId: 2,
      },
    });

    await waitFor(() => {
      expect(getApplicationTrackerRow).toHaveBeenCalledWith(2);
      expect(screen.getByLabelText("Job opportunity")).toHaveValue("2");
    });
    expect(screen.queryByRole("heading", {
      name: "Edit Operations Engineer",
    })).not.toBeInTheDocument();
  });

  it("opens the existing editor for an APPLY handoff when an Application exists", async () => {
    renderHandoff({
      dashboardHandoff: {
        action: "APPLY",
        jobOpportunityId: 1,
      },
    });

    expect(await screen.findByRole("heading", {
      name: "Edit Platform Engineer",
    })).toBeInTheDocument();
    expect(screen.getByLabelText("Application Details editor"))
      .toHaveFocus();
  });

  it.each([
    ["FINISH_APPLICATION", "Application Details editor", null],
    ["FOLLOW_UP", "Follow-up date", "followUpDate"],
    ["PREPARE_INTERVIEW", "Interview topics", "interviewTopics"],
  ])("focuses the intended editor field for %s", async (
    action,
    label,
    fieldName,
  ) => {
    renderHandoff({
      dashboardHandoff: {
        action,
        jobOpportunityId: 1,
        applicationId: 101,
      },
    });

    const target = await screen.findByLabelText(label);
    await waitFor(() => {
      if (fieldName === null) {
        expect(target).toHaveFocus();
      } else {
        expect(document.activeElement).toHaveAttribute("name", fieldName);
      }
    });
  });

  it("falls back safely when the Dashboard job was deleted", async () => {
    vi.mocked(getApplicationTrackerRow).mockRejectedValue(
      new Error("Job opportunity 404 was not found"),
    );
    renderHandoff({
      dashboardHandoff: {
        action: "APPLY",
        jobOpportunityId: 404,
      },
    });

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "job no longer exists",
    );
    expect(screen.getByRole("table")).toBeInTheDocument();
  });

  it("does not open a different Application when a handoff is stale", async () => {
    renderHandoff({
      dashboardHandoff: {
        action: "FOLLOW_UP",
        jobOpportunityId: 1,
        applicationId: 999,
      },
    });

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "application that no longer exists",
    );
    expect(screen.queryByRole("heading", {
      name: "Edit Platform Engineer",
    })).not.toBeInTheDocument();
    expect(screen.getByRole("table")).toBeInTheDocument();
  });

  it("ignores non-allow-listed handoff actions and keeps normal tracker behavior", async () => {
    renderHandoff({
      dashboardHandoff: {
        action: "DELETE_EVERYTHING",
        jobOpportunityId: 1,
      },
    });

    expect(await screen.findByRole("table")).toBeInTheDocument();
    expect(getApplicationTrackerRow).not.toHaveBeenCalled();
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });
});
