import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import JobList from "./JobList";
import type { JobOpportunity } from "../types/job";

const job: JobOpportunity = {
  id: 1,
  companyId: 1,
  companyName: "GitHub",
  positionTitle: "Junior Software Engineer",
  department: "Engineering",
  location: "Remote",
  remoteType: "REMOTE",
  employmentType: "Full-time",
  salaryMin: 95000,
  salaryMax: 125000,
  salaryCurrency: "USD",
  salaryNotes: null,
  applicationUrl:
    "https://example.com/jobs/software-engineer",
  source: "Company website",
  datePosted: "2026-08-06",
  closingDate: null,
  priority: 1,
  matchScore: 9,
  jobDescription:
    "Build and maintain full-stack software.",
  notes: "Updated test job.",
  createdAt: "2026-08-06T18:44:00Z",
  updatedAt: "2026-08-06T19:19:14Z",
};

describe("JobList", () => {
  it("displays job information", () => {
    render(
      <JobList
        jobs={[job]}
        deletingId={null}
        onEdit={vi.fn()}
        onDelete={vi.fn().mockResolvedValue(undefined)}
      />,
    );

    expect(
      screen.getByRole("heading", {
        name: "Junior Software Engineer",
      }),
    ).toBeInTheDocument();

    expect(screen.getByText("GitHub")).toBeInTheDocument();
    expect(screen.getByText("Priority 1")).toBeInTheDocument();
    expect(screen.getByText("REMOTE")).toBeInTheDocument();
    expect(screen.getByText("9/10")).toBeInTheDocument();

    expect(
      screen.getByRole("link", {
        name: "View application",
      }),
    ).toHaveAttribute(
      "href",
      "https://example.com/jobs/software-engineer",
    );
  });

  it("displays an empty state", () => {
    render(
      <JobList
        jobs={[]}
        deletingId={null}
        onEdit={vi.fn()}
        onDelete={vi.fn().mockResolvedValue(undefined)}
      />,
    );

    expect(
      screen.getByText("No jobs found."),
    ).toBeInTheDocument();
  });

  it("requests editing for a job", async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();

    render(
      <JobList
        jobs={[job]}
        deletingId={null}
        onEdit={onEdit}
        onDelete={vi.fn().mockResolvedValue(undefined)}
      />,
    );

    await user.click(
      screen.getByRole("button", { name: "Edit" }),
    );

    expect(onEdit).toHaveBeenCalledWith(job);
  });

  it("requests deletion for a job", async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn().mockResolvedValue(undefined);

    render(
      <JobList
        jobs={[job]}
        deletingId={null}
        onEdit={vi.fn()}
        onDelete={onDelete}
      />,
    );

    await user.click(
      screen.getByRole("button", { name: "Delete" }),
    );

    expect(onDelete).toHaveBeenCalledWith(job);
  });
});