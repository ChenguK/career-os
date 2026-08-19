import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import ApplicationForm from "./ApplicationForm";
import type { Application } from "../types/application";
import type { JobOpportunity } from "../../jobs/types/job";

const job: JobOpportunity = {
  id: 2,
  companyId: 1,
  companyName: "GitHub",
  positionTitle: "Frontend Engineer",
  department: "Engineering",
  location: "Remote",
  remoteType: "REMOTE",
  employmentType: "Full-time",
  salaryMin: 90000,
  salaryMax: 120000,
  salaryCurrency: "USD",
  salaryNotes: null,
  applicationUrl: null,
  source: null,
  datePosted: null,
  closingDate: null,
  priority: 1,
  matchScore: 8.5,
  jobDescription: null,
  notes: null,
  createdAt: "2026-08-08T01:00:00Z",
  updatedAt: "2026-08-08T01:00:00Z",
};

const createdApplication: Application = {
  id: 2,
  jobOpportunityId: 2,
  positionTitle: "Frontend Engineer",
  companyId: 1,
  companyName: "GitHub",
  status: "APPLIED",
  resumeVersion: "Software Engineering",
  coverLetterNeeded: true,
  portfolioLink: "",
  githubLink: "",
  projectsToHighlight: "",
  skillsToEmphasize: "",
  interviewTopics: "",
  recruiterName: "Taylor Recruiter",
  recruiterEmail: "taylor@example.com",
  applicationDate: "2026-08-07",
  followUpDate: "2026-08-14",
  phoneScreenAt: null,
  interviewOneAt: null,
  interviewTwoAt: null,
  offerAt: null,
  rejectedAt: null,
  notes: null,
  createdAt: "2026-08-08T01:00:00Z",
  updatedAt: "2026-08-08T01:00:00Z",
};

describe("ApplicationForm", () => {
  it("submits entered application information", async () => {
    const user = userEvent.setup();

    const onSubmit =
      vi.fn().mockResolvedValue(createdApplication);

    render(
      <ApplicationForm
        heading="Add application"
        submitLabel="Add application"
        jobs={[job]}
        resumeMaterials={[{id:7,applicantProfileId:1,materialType:"RESUME",displayName:"Software Engineering Resume",originalFilename:"resume.pdf",mimeType:"application/pdf",fileSize:1000,active:true,notes:null,targetJobFamily:"Software Engineering",targetSeniority:null,versionLabel:null,profileDefault:true,createdAt:"2026-08-01T00:00:00Z",updatedAt:"2026-08-01T00:00:00Z"}]}
        onSubmit={onSubmit}
      />,
    );

    await user.selectOptions(
      screen.getByLabelText("Job opportunity"),
      "2",
    );

    await user.selectOptions(
      screen.getByLabelText("Status"),
      "PREPARING",
    );

    await user.selectOptions(
      screen.getByLabelText("Résumé version"),
      "7",
    );

    await user.click(
      screen.getByLabelText("Cover letter needed"),
    );

    await user.type(
      screen.getByLabelText("Recruiter name"),
      "Taylor Recruiter",
    );

    await user.type(
      screen.getByLabelText("Recruiter email"),
      "taylor@example.com",
    );

    await user.type(
      screen.getByLabelText("Application date"),
      "2026-08-07",
    );

    await user.type(
      screen.getByLabelText("Follow-up date"),
      "2026-08-14",
    );

    await user.click(
      screen.getByRole("button", {
        name: "Add application",
      }),
    );

    expect(onSubmit).toHaveBeenCalledOnce();

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        jobOpportunityId: 2,
        status: "PREPARING",
        resumeMaterialId: 7,
        coverLetterNeeded: true,
        recruiterName: "Taylor Recruiter",
        recruiterEmail: "taylor@example.com",
        applicationDate: "2026-08-07",
        followUpDate: "2026-08-14",
      }),
    );
  });

  it("clears the create form after successful submission", async () => {
    const user = userEvent.setup();

    const onSubmit =
      vi.fn().mockResolvedValue(createdApplication);

    render(
      <ApplicationForm
        heading="Add application"
        submitLabel="Add application"
        jobs={[job]}
        onSubmit={onSubmit}
      />,
    );

    await user.selectOptions(
      screen.getByLabelText("Job opportunity"),
      "2",
    );

    await user.type(
      screen.getByLabelText("Recruiter name"),
      "Taylor Recruiter",
    );

    await user.click(
      screen.getByRole("button", {
        name: "Add application",
      }),
    );

    expect(
      screen.getByLabelText("Recruiter name"),
    ).toHaveValue("");

    expect(
      screen.getByLabelText("Job opportunity"),
    ).toHaveValue("");
  });
});
