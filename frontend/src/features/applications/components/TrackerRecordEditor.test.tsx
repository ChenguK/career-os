import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import TrackerRecordEditor from "./TrackerRecordEditor";
import type { ApplicationTrackerRow } from "../types/applicationTracker";

const row: ApplicationTrackerRow = {
  jobOpportunityId: 11,
  companyId: 1,
  companyName: "GitHub",
  positionTitle: "Platform Engineer",
  department: "Engineering",
  location: "New York, NY",
  remoteType: "HYBRID",
  employmentType: "Full-time",
  salaryMin: 90000,
  salaryMax: 120000,
  salaryCurrency: "USD",
  salaryNotes: "Plus bonus",
  applicationUrl: "https://example.com/job",
  source: "Company site",
  datePosted: "2026-08-01",
  closingDate: "2026-08-31",
  priority: 1,
  matchScore: 8.5,
  jobDescription: "Build the platform",
  jobNotes: "Job notes",
  jobCreatedAt: "2026-08-01T10:00:00Z",
  jobUpdatedAt: "2026-08-02T10:00:00Z",
  applicationId: 101,
  status: "PHONE_SCREEN",
  resumeVersion: "Software Engineering",
  coverLetterNeeded: true,
  portfolioLink: "https://portfolio.test",
  githubLink: "https://github.test",
  projectsToHighlight: "CareerOS",
  skillsToEmphasize: "Java and React",
  interviewTopics: "System design",
  recruiterName: "Ada Recruiter",
  recruiterEmail: "ada@example.com",
  applicationDate: "2026-08-07",
  followUpDate: "2026-08-14",
  phoneScreenAt: "2026-08-12T14:00:00Z",
  interviewOneAt: "2026-08-15T14:00:00Z",
  interviewTwoAt: "2026-08-18T14:00:00Z",
  offerAt: "2026-08-20T14:00:00Z",
  rejectedAt: "2026-08-21T14:00:00Z",
  applicationNotes: "Application notes",
  applicationCreatedAt: "2026-08-07T10:00:00Z",
  applicationUpdatedAt: "2026-08-08T10:00:00Z",
};

const company = {
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
};

const job = {
  id: 11,
  companyId: 1,
  companyName: "GitHub",
  positionTitle: row.positionTitle,
  department: row.department,
  location: row.location,
  remoteType: row.remoteType,
  employmentType: row.employmentType,
  salaryMin: row.salaryMin,
  salaryMax: row.salaryMax,
  salaryCurrency: row.salaryCurrency,
  salaryNotes: row.salaryNotes,
  applicationUrl: row.applicationUrl,
  source: row.source,
  datePosted: row.datePosted,
  closingDate: row.closingDate,
  priority: row.priority,
  matchScore: row.matchScore,
  jobDescription: row.jobDescription,
  notes: row.jobNotes,
  createdAt: row.jobCreatedAt,
  updatedAt: row.jobUpdatedAt,
};

function renderEditor(source = row) {
  const props = {
    row: source,
    companies: [company],
    jobs: [job],
    onSaveJob: vi.fn().mockResolvedValue(job),
    onSaveApplication: vi.fn().mockResolvedValue({}),
    onAddApplication: vi.fn(),
    onClose: vi.fn(),
  };
  render(<TrackerRecordEditor {...props} />);
  return props;
}

describe("TrackerRecordEditor", () => {
  it("populates every job and application field from the canonical row", () => {
    renderEditor();

    expect(screen.getByLabelText("Company")).toHaveValue("1");
    expect(screen.getByLabelText("Position title")).toHaveValue("Platform Engineer");
    expect(screen.getByLabelText("Department")).toHaveValue("Engineering");
    expect(screen.getByLabelText("Location")).toHaveValue("New York, NY");
    expect(screen.getByLabelText("Work arrangement")).toHaveValue("HYBRID");
    expect(screen.getByLabelText("Employment type")).toHaveValue("Full-time");
    expect(screen.getByLabelText("Minimum salary")).toHaveValue(90000);
    expect(screen.getByLabelText("Maximum salary")).toHaveValue(120000);
    expect(screen.getByLabelText("Currency")).toHaveValue("USD");
    expect(screen.getByLabelText("Salary notes")).toHaveValue("Plus bonus");
    expect(screen.getByLabelText("Application URL")).toHaveValue(row.applicationUrl);
    expect(screen.getByLabelText("Source")).toHaveValue("Company site");
    expect(screen.getByLabelText("Date posted")).toHaveValue("2026-08-01");
    expect(screen.getByLabelText("Closing date")).toHaveValue("2026-08-31");
    expect(screen.getByLabelText("Priority")).toHaveValue(1);
    expect(screen.getByLabelText("Match score")).toHaveValue(8.5);
    expect(screen.getByLabelText("Job description")).toHaveValue("Build the platform");

    expect(screen.getByLabelText("Status")).toHaveValue("PHONE_SCREEN");
    expect(screen.getByLabelText("Résumé version")).toHaveValue("Software Engineering");
    expect(screen.getByLabelText("Cover letter needed")).toBeChecked();
    expect(screen.getByLabelText("Portfolio link")).toHaveValue("https://portfolio.test");
    expect(screen.getByLabelText("GitHub link")).toHaveValue("https://github.test");
    expect(screen.getByLabelText("Projects to highlight")).toHaveValue("CareerOS");
    expect(screen.getByLabelText("Skills to emphasize")).toHaveValue("Java and React");
    expect(screen.getByLabelText("Interview topics")).toHaveValue("System design");
    expect(screen.getByLabelText("Recruiter name")).toHaveValue("Ada Recruiter");
    expect(screen.getByLabelText("Recruiter email")).toHaveValue("ada@example.com");
    expect(screen.getByLabelText("Application date")).toHaveValue("2026-08-07");
    expect(screen.getByLabelText("Follow-up date")).toHaveValue("2026-08-14");

    const notes = screen.getAllByLabelText("Notes");
    expect(notes[0]).toHaveValue("Job notes");
    expect(notes[1]).toHaveValue("Application notes");
  });

  it("saves each normalized section independently", async () => {
    const user = userEvent.setup();
    const props = renderEditor();

    await user.click(screen.getByRole("button", { name: "Save Job Details" }));
    expect(props.onSaveJob).toHaveBeenCalledTimes(1);
    expect(props.onSaveApplication).not.toHaveBeenCalled();
    expect(await screen.findByText("Job details saved.")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Save Application Details" }));
    expect(props.onSaveApplication).toHaveBeenCalledTimes(1);
    expect(props.onSaveJob).toHaveBeenCalledTimes(1);
    expect(await screen.findByText("Application details saved."))
      .toBeInTheDocument();
  });

  it("offers Cancel beside both forms and closes without saving", async () => {
    const user = userEvent.setup();
    const props = renderEditor();
    const cancelButtons = screen.getAllByRole("button", { name: "Cancel" });

    expect(cancelButtons).toHaveLength(2);
    await user.click(cancelButtons[0]);
    expect(props.onClose).toHaveBeenCalledTimes(1);
    expect(props.onSaveJob).not.toHaveBeenCalled();
    expect(props.onSaveApplication).not.toHaveBeenCalled();
  });

  it("edits job-only rows without creating an application", async () => {
    const user = userEvent.setup();
    const props = renderEditor({ ...row, applicationId: null, status: null });

    expect(screen.getByText("No Application exists for this job yet."))
      .toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Cancel" }))
      .toBeInTheDocument();
    expect(props.onSaveApplication).not.toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Add Application" }));
    expect(props.onAddApplication).toHaveBeenCalledWith(11);
  });

  it("preserves the other section's unsaved values when one save fails", async () => {
    const user = userEvent.setup();
    const props = renderEditor();
    props.onSaveJob.mockRejectedValueOnce(new Error("Job save failed"));

    const recruiter = screen.getByLabelText("Recruiter name");
    await user.clear(recruiter);
    await user.type(recruiter, "Unsaved Recruiter");
    await user.click(screen.getByRole("button", { name: "Save Job Details" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Job save failed");
    expect(recruiter).toHaveValue("Unsaved Recruiter");
    expect(props.onSaveApplication).not.toHaveBeenCalled();
  });
});
