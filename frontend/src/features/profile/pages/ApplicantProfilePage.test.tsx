import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import ApplicantProfilePage from "./ApplicantProfilePage";
import {
  getApplicantProfile,
  saveApplicantProfile,
  verifyApplicantProfile,
} from "../api/applicantProfileApi";
import type { ApplicantProfile } from "../types/applicantProfile";

vi.mock("../api/applicantProfileApi", () => ({
  getApplicantProfile: vi.fn(),
  saveApplicantProfile: vi.fn(),
  verifyApplicantProfile: vi.fn(),
}));

const emptyProfile: ApplicantProfile = {
  exists: false, id: null, firstName: null, lastName: null,
  preferredName: null, email: null, phone: null, city: null,
  stateRegion: null, country: null, postalCode: null, portfolioUrl: null,
  githubUrl: null, linkedinUrl: null, defaultResumeVersion: null,
  preferredWorkArrangement: "UNKNOWN", minimumSalary: null,
  salaryCurrency: "USD", willingToRelocate: null, willingToTravel: null,
  verified: false, lastVerifiedAt: null, createdAt: null, updatedAt: null,
};

const savedProfile: ApplicantProfile = {
  ...emptyProfile,
  exists: true,
  id: 1,
  firstName: "Chengu",
  lastName: "Kargbo",
  preferredName: "CK",
  email: "chengu@example.com",
  city: "New York",
  stateRegion: "NY",
  country: "United States",
  postalCode: "10001",
  portfolioUrl: "https://portfolio.example",
  githubUrl: "https://github.com/chengu",
  linkedinUrl: "https://linkedin.com/in/chengu",
  defaultResumeVersion: "Software Engineering",
  preferredWorkArrangement: "REMOTE",
  minimumSalary: 120000,
  willingToRelocate: true,
  willingToTravel: false,
  createdAt: "2026-08-18T12:00:00Z",
  updatedAt: "2026-08-18T12:00:00Z",
};

describe("ApplicantProfilePage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(getApplicantProfile).mockResolvedValue(savedProfile);
    vi.mocked(saveApplicantProfile).mockResolvedValue(savedProfile);
    vi.mocked(verifyApplicantProfile).mockResolvedValue({
      ...savedProfile,
      verified: true,
      lastVerifiedAt: "2026-08-18T13:00:00Z",
      updatedAt: "2026-08-18T13:00:00Z",
    });
  });

  it("shows loading and then populates all profile sections", async () => {
    render(<ApplicantProfilePage />);
    expect(screen.getByText("Loading applicant profile...")).toBeInTheDocument();

    expect(await screen.findByRole("heading", { name: "Applicant Profile" }))
      .toBeInTheDocument();
    expect(screen.getByText("Contact")).toBeInTheDocument();
    expect(screen.getByText("Professional Links")).toBeInTheDocument();
    expect(screen.getByText("Work Preferences")).toBeInTheDocument();
    expect(screen.getByText("Application Defaults")).toBeInTheDocument();
    expect(screen.getByLabelText("First name")).toHaveValue("Chengu");
    expect(screen.getByLabelText("Preferred work arrangement"))
      .toHaveValue("REMOTE");
    expect(screen.getByLabelText("Willing to relocate")).toHaveValue("true");
  });

  it("supports the empty state and creates the first profile", async () => {
    vi.mocked(getApplicantProfile).mockResolvedValue(emptyProfile);
    const user = userEvent.setup();
    render(<ApplicantProfilePage />);
    expect(await screen.findByText("No applicant profile has been saved yet."))
      .toBeInTheDocument();

    await user.type(screen.getByLabelText("First name"), "Chengu");
    await user.type(screen.getByLabelText("Last name"), "Kargbo");
    await user.type(screen.getByLabelText("Email"), "chengu@example.com");
    await user.click(screen.getByRole("button", { name: "Save profile" }));

    expect(saveApplicantProfile).toHaveBeenCalledWith(expect.objectContaining({
      firstName: "Chengu", lastName: "Kargbo", email: "chengu@example.com",
    }));
    expect(await screen.findByText(/Verify it before future autofill use/))
      .toBeInTheDocument();
  });

  it("uses browser validation for required identity fields", async () => {
    vi.mocked(getApplicantProfile).mockResolvedValue(emptyProfile);
    const user = userEvent.setup();
    render(<ApplicantProfilePage />);
    await screen.findByText("No applicant profile has been saved yet.");

    await user.click(screen.getByRole("button", { name: "Save profile" }));

    expect(saveApplicantProfile).not.toHaveBeenCalled();
    expect(screen.getByLabelText("First name")).toBeInvalid();
  });

  it("marks only the current saved profile verified through explicit action", async () => {
    const user = userEvent.setup();
    render(<ApplicantProfilePage />);
    await screen.findByLabelText("First name");

    await user.click(screen.getByRole("button", {
      name: "Verify current profile",
    }));

    expect(verifyApplicantProfile).toHaveBeenCalledTimes(1);
    expect(await screen.findByText(/Verified Aug 18, 2026/)).toBeInTheDocument();
  });

  it("shows load and save errors without affecting other workflows", async () => {
    vi.mocked(getApplicantProfile).mockRejectedValueOnce(
      new Error("Profile service unavailable"),
    );
    const { unmount } = render(<ApplicantProfilePage />);
    expect(await screen.findByRole("alert"))
      .toHaveTextContent("Profile service unavailable");
    unmount();

    vi.mocked(getApplicantProfile).mockResolvedValue(savedProfile);
    vi.mocked(saveApplicantProfile).mockRejectedValueOnce(
      new Error("Profile could not be saved"),
    );
    const user = userEvent.setup();
    render(<ApplicantProfilePage />);
    await screen.findByLabelText("First name");
    await user.click(screen.getByRole("button", { name: "Save profile" }));
    await waitFor(() => expect(screen.getByRole("alert"))
      .toHaveTextContent("Profile could not be saved"));
  });
});
