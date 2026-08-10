import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { ComponentProps } from "react";

import ApplicationList from "./ApplicationList";
import type { ApplicationTrackerRow } from "../types/applicationTracker";

const applicationRow: ApplicationTrackerRow = {
  jobOpportunityId: 11,
  companyId: 1,
  companyName: "GitHub",
  positionTitle: "Junior Software Engineer",
  department: "Engineering",
  location: "New York, NY",
  remoteType: "HYBRID",
  employmentType: "Full-time",
  salaryMin: 90000,
  salaryMax: 120000,
  salaryCurrency: "USD",
  salaryNotes: "Plus bonus",
  applicationUrl: "https://example.com/jobs/11",
  source: "Company site",
  datePosted: "2026-08-01",
  closingDate: "2026-08-31",
  priority: 1,
  matchScore: 8.5,
  jobDescription: "Build software",
  jobNotes: "Private job notes",
  jobCreatedAt: "2026-08-01T10:00:00Z",
  jobUpdatedAt: "2026-08-02T10:00:00Z",
  applicationId: 101,
  status: "PHONE_SCREEN",
  resumeVersion: "Software Engineering",
  coverLetterNeeded: false,
  portfolioLink: "https://portfolio.test",
  githubLink: "https://github.com/test",
  projectsToHighlight: "CareerOS",
  skillsToEmphasize: "Java and React",
  interviewTopics: "System design",
  recruiterName: "Test Recruiter",
  recruiterEmail: "recruiter@example.com",
  applicationDate: "2026-08-07",
  followUpDate: "2026-08-14",
  phoneScreenAt: "2026-08-12T14:00:00Z",
  interviewOneAt: null,
  interviewTwoAt: null,
  offerAt: null,
  rejectedAt: null,
  applicationNotes: "Private application notes",
  applicationCreatedAt: "2026-08-07T10:00:00Z",
  applicationUpdatedAt: "2026-08-08T10:00:00Z",
};

const jobOnlyRow: ApplicationTrackerRow = {
  ...applicationRow,
  jobOpportunityId: 12,
  companyId: null,
  companyName: null,
  positionTitle: "Operations Engineer",
  location: null,
  remoteType: "UNKNOWN",
  employmentType: null,
  salaryMin: null,
  salaryMax: null,
  applicationUrl: null,
  source: null,
  datePosted: null,
  matchScore: null,
  applicationId: null,
  status: null,
  resumeVersion: null,
  coverLetterNeeded: null,
  applicationDate: null,
  followUpDate: null,
  applicationNotes: null,
  applicationCreatedAt: null,
  applicationUpdatedAt: null,
};

function renderList(
  rows: ApplicationTrackerRow[],
  overrides: Partial<
    ComponentProps<typeof ApplicationList>
  > = {},
) {
  const props: ComponentProps<typeof ApplicationList> = {
    rows,
    deletingId: null,
    onEdit: vi.fn(),
    onDelete: vi.fn().mockResolvedValue(undefined),
    onAddApplication: vi.fn(),
    ...overrides,
  };

  render(<ApplicationList {...props} />);
  return props;
}

describe("ApplicationList", () => {
  it("renders essential job and application fields in a table", () => {
    renderList([applicationRow]);

    expect(screen.getByRole("table")).toBeInTheDocument();
    expect(screen.getByText("GitHub")).toBeInTheDocument();
    expect(
      screen.getByRole("rowheader", {
        name: "Junior Software Engineer",
      }),
    ).toBeInTheDocument();
    expect(screen.getByText("Phone Screen")).toBeInTheDocument();
    expect(screen.getByText("Hybrid")).toBeInTheDocument();
    expect(screen.getByText("Full-time")).toBeInTheDocument();
    expect(screen.getByText("8.5/10")).toBeInTheDocument();
    expect(screen.getByText("Software Engineering")).toBeInTheDocument();
    expect(screen.getByText("Company site")).toBeInTheDocument();
    expect(screen.getByText("No")).toBeInTheDocument();
    expect(screen.getByText("Aug 7, 2026")).toBeInTheDocument();
    expect(screen.getByText("Aug 14, 2026")).toBeInTheDocument();
    expect(
      screen.getByText("$90,000–$120,000"),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", { name: "Open job" }),
    ).toHaveAttribute("href", "https://example.com/jobs/11");

    expect(screen.queryByText("Private job notes"))
      .not.toBeInTheDocument();
    expect(screen.queryByText("Private application notes"))
      .not.toBeInTheDocument();
  });

  it("renders a job-only row safely with a saved lifecycle", () => {
    renderList([jobOnlyRow]);

    expect(screen.getByText("Operations Engineer"))
      .toBeInTheDocument();
    expect(screen.getByText("Saved")).toBeInTheDocument();
    expect(screen.getByText("Unknown")).toBeInTheDocument();
    expect(
      screen.getByRole("button", {
        name: "Add application for Operations Engineer",
      }),
    ).toBeInTheDocument();
  });

  it("targets the correct application for edit and delete", async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();
    const onDelete = vi.fn().mockResolvedValue(undefined);

    renderList([applicationRow], { onEdit, onDelete });

    await user.click(
      screen.getByRole("button", {
        name: "Edit application for Junior Software Engineer",
      }),
    );
    await user.click(
      screen.getByRole("button", {
        name: "Delete application for Junior Software Engineer",
      }),
    );

    expect(onEdit).toHaveBeenCalledWith(101);
    expect(onDelete).toHaveBeenCalledWith(101);
  });

  it("targets the correct job when adding an application", async () => {
    const user = userEvent.setup();
    const onAddApplication = vi.fn();

    renderList([jobOnlyRow], { onAddApplication });

    await user.click(
      screen.getByRole("button", {
        name: "Add application for Operations Engineer",
      }),
    );

    expect(onAddApplication).toHaveBeenCalledWith(12);
  });

  it("displays an empty tracker state", () => {
    renderList([]);

    expect(screen.getByText("No tracked jobs found."))
      .toBeInTheDocument();
  });
});
