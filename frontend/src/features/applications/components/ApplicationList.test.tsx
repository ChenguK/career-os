import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import ApplicationList from "./ApplicationList";
import type { Application } from "../types/application";

const application: Application = {
  id: 1,
  jobOpportunityId: 1,
  positionTitle: "Junior Software Engineer",
  companyId: 1,
  companyName: "GitHub",
  status: "PHONE_SCREEN",
  resumeVersion: "Software Engineering",
  coverLetterNeeded: false,
  portfolioLink: "https://chengucodes.dev",
  githubLink: "https://github.com/ChenguK",
  projectsToHighlight:
    "Career OS, Working Actor OS, DevCommands",
  skillsToEmphasize:
    "Java, Spring Boot, React, TypeScript, PostgreSQL",
  interviewTopics:
    "REST APIs, application architecture, testing",
  recruiterName: "Test Recruiter",
  recruiterEmail: "recruiter@example.com",
  applicationDate: "2026-08-07",
  followUpDate: "2026-08-14",
  phoneScreenAt: "2026-08-12T14:00:00Z",
  interviewOneAt: null,
  interviewTwoAt: null,
  offerAt: null,
  rejectedAt: null,
  notes: "Phone screen scheduled.",
  createdAt: "2026-08-08T01:56:39Z",
  updatedAt: "2026-08-08T01:57:50Z",
};

describe("ApplicationList", () => {
  it("displays application information", () => {
    render(
      <ApplicationList
        applications={[application]}
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

    expect(
        screen.getByText("GitHub", {
            selector: "p",
        }),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Phone Screen"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Software Engineering"),
    ).toBeInTheDocument();

    expect(
      screen.getByText("Test Recruiter"),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("link", { name: "Portfolio" }),
    ).toHaveAttribute("href", "https://chengucodes.dev");

    expect(
      screen.getByRole("link", { name: "GitHub" }),
    ).toHaveAttribute(
      "href",
      "https://github.com/ChenguK",
        );
    });

  it("displays an empty state", () => {
    render(
      <ApplicationList
        applications={[]}
        deletingId={null}
        onEdit={vi.fn()}
        onDelete={vi.fn().mockResolvedValue(undefined)}
      />,
    );

    expect(
      screen.getByText("No applications found."),
    ).toBeInTheDocument();
  });

  it("requests editing for an application", async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();

    render(
      <ApplicationList
        applications={[application]}
        deletingId={null}
        onEdit={onEdit}
        onDelete={vi.fn().mockResolvedValue(undefined)}
      />,
    );

    await user.click(
      screen.getByRole("button", { name: "Edit" }),
    );

    expect(onEdit).toHaveBeenCalledWith(application);
  });

  it("requests deletion for an application", async () => {
    const user = userEvent.setup();
    const onDelete = vi.fn().mockResolvedValue(undefined);

    render(
      <ApplicationList
        applications={[application]}
        deletingId={null}
        onEdit={vi.fn()}
        onDelete={onDelete}
      />,
    );

    await user.click(
      screen.getByRole("button", { name: "Delete" }),
    );

    expect(onDelete).toHaveBeenCalledWith(application);
  });
});