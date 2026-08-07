import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import JobForm from "./JobForm";
import type { Company } from "../../companies/types/company";
import type { JobOpportunity } from "../types/job";

const github: Company = {
  id: 1,
  name: "GitHub",
  websiteUrl: "https://github.com",
  careersUrl: "https://www.github.careers",
  industry: "Developer Platform",
  companyType: "SaaS",
  mission: null,
  products: null,
  techStack: null,
  remotePolicy: null,
  salaryNotes: null,
  generalNotes: null,
  dreamCompany: true,
  createdAt: "2026-08-04T11:10:46Z",
  updatedAt: "2026-08-04T12:10:59Z",
};

const createdJob: JobOpportunity = {
  id: 2,
  companyId: 1,
  companyName: "GitHub",
  positionTitle: "Frontend Engineer",
  department: null,
  location: "Remote",
  remoteType: "REMOTE",
  employmentType: null,
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
  createdAt: "2026-08-06T20:00:00Z",
  updatedAt: "2026-08-06T20:00:00Z",
};

describe("JobForm", () => {
  it("submits entered job information", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(createdJob);

    render(
      <JobForm
        heading="Add job"
        submitLabel="Add job"
        companies={[github]}
        onSubmit={onSubmit}
/>
    );

    await user.selectOptions(
      screen.getByLabelText("Company"),
      "1",
    );

    await user.type(
      screen.getByLabelText("Position title"),
      "Frontend Engineer",
    );

    await user.selectOptions(
      screen.getByLabelText("Work arrangement"),
      "REMOTE",
    );

    await user.clear(screen.getByLabelText("Priority"));
    await user.type(screen.getByLabelText("Priority"), "2");

    await user.type(
      screen.getByLabelText("Match score"),
      "8.5",
    );

    await user.click(
      screen.getByRole("button", { name: "Add job" }),
    );

    expect(onSubmit).toHaveBeenCalledOnce();

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        companyId: 1,
        positionTitle: "Frontend Engineer",
        remoteType: "REMOTE",
        priority: 2,
        matchScore: 8.5,
      }),
    );
  });

  it("clears the form after successful submission", async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(createdJob);

    render(
      <JobForm
        heading="Add job"
        submitLabel="Add job"
        companies={[github]}
        onSubmit={onSubmit}
        />
    );

    const titleInput =
      screen.getByLabelText("Position title");

    await user.type(titleInput, "Frontend Engineer");

    await user.click(
      screen.getByRole("button", { name: "Add job" }),
    );

    expect(titleInput).toHaveValue("");
  });
});