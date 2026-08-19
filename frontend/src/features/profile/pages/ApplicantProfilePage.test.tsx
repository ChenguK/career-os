import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";

import ApplicantProfilePage from "./ApplicantProfilePage";
import {
  getApplicantProfile,
  saveApplicantProfile,
  verifyApplicantProfile,
} from "../api/applicantProfileApi";
import type { ApplicantProfile } from "../types/applicantProfile";
import { getCareerMaterials } from "../api/careerMaterialsApi";

vi.mock("../api/applicantProfileApi", () => ({
  getApplicantProfile: vi.fn(),
  saveApplicantProfile: vi.fn(),
  verifyApplicantProfile: vi.fn(),
}));
vi.mock("../api/careerMaterialsApi",()=>({getCareerMaterials:vi.fn(),uploadCareerMaterial:vi.fn(),setDefaultCareerMaterial:vi.fn(),deactivateCareerMaterial:vi.fn(),deleteCareerMaterial:vi.fn(),careerMaterialDownloadUrl:(id:number)=>`/api/applicant-profile/materials/${id}/download`}));

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
  const renderPage = () => render(<MemoryRouter><ApplicantProfilePage /></MemoryRouter>);
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(getApplicantProfile).mockResolvedValue(savedProfile);
    vi.mocked(getCareerMaterials).mockResolvedValue([]);
    vi.mocked(saveApplicantProfile).mockResolvedValue(savedProfile);
    vi.mocked(verifyApplicantProfile).mockResolvedValue({
      ...savedProfile,
      verified: true,
      lastVerifiedAt: "2026-08-18T13:00:00Z",
      updatedAt: "2026-08-18T13:00:00Z",
    });
  });

  it("shows loading and then populates all profile sections", async () => {
    renderPage();
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
    expect(screen.getByText("0 Career Materials")).toBeInTheDocument();
  });

  it("supports the empty state and creates the first profile", async () => {
    vi.mocked(getApplicantProfile).mockResolvedValue(emptyProfile);
    const user = userEvent.setup();
    renderPage();
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
    renderPage();
    await screen.findByText("No applicant profile has been saved yet.");

    await user.click(screen.getByRole("button", { name: "Save profile" }));

    expect(saveApplicantProfile).not.toHaveBeenCalled();
    expect(screen.getByLabelText("First name")).toBeInvalid();
  });

  it("marks only the current saved profile verified through explicit action", async () => {
    const user = userEvent.setup();
    renderPage();
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
    const { unmount } = renderPage();
    expect(await screen.findByRole("alert"))
      .toHaveTextContent("Profile service unavailable");
    unmount();

    vi.mocked(getApplicantProfile).mockResolvedValue(savedProfile);
    vi.mocked(saveApplicantProfile).mockRejectedValueOnce(
      new Error("Profile could not be saved"),
    );
    const user = userEvent.setup();
    renderPage();
    await screen.findByLabelText("First name");
    await user.click(screen.getByRole("button", { name: "Save profile" }));
    await waitFor(() => expect(screen.getByRole("alert"))
      .toHaveTextContent("Profile could not be saved"));
  });

  it("shows all five materials without overflow at the profile limit", async () => {
    vi.mocked(getCareerMaterials).mockResolvedValue(Array.from({ length: 5 }, (_, index) => ({
      id: index + 1, applicantProfileId: 1, materialType: "RESUME",
      displayName: `Resume ${index + 1}`, originalFilename: `resume-${index + 1}.pdf`,
      mimeType: "application/pdf", fileSize: 1024, active: true, notes: null,
      targetJobFamily: null, targetSeniority: null, versionLabel: null,
      profileDefault: index === 0, createdAt: "2026-01-01T00:00:00Z",
      updatedAt: "2026-01-01T00:00:00Z",
    })));
    renderPage();
    expect(await screen.findByText("5 Career Materials")).toBeInTheDocument();
    expect(screen.getByText("Resume 5")).toBeInTheDocument();
    expect(screen.queryByText(/Showing 5 of/)).not.toBeInTheDocument();
  });

  it("caps a crowded profile summary and links to full management", async () => {
    vi.mocked(getCareerMaterials).mockResolvedValue(Array.from({ length: 6 }, (_, index) => ({
      id: index + 1, applicantProfileId: 1, materialType: "RESUME",
      displayName: `Resume ${index + 1}`, originalFilename: `resume-${index + 1}.pdf`,
      mimeType: "application/pdf", fileSize: 1024, active: index < 5, notes: null,
      targetJobFamily: null, targetSeniority: null, versionLabel: null,
      profileDefault: false, createdAt: "2026-01-01T00:00:00Z",
      updatedAt: "2026-01-01T00:00:00Z",
    })));
    renderPage();
    expect(await screen.findByText("6 Career Materials")).toBeInTheDocument();
    expect(screen.getByText("Showing 5 of 6.")).toBeInTheDocument();
    expect(screen.queryByText("Resume 6")).not.toBeInTheDocument();
    expect(screen.getByRole("link", { name: "View all materials" })).toHaveAttribute("href", "/materials");
    expect(screen.getByRole("link", { name: "Manage Materials" })).toHaveAttribute("href", "/materials");
  });
});
