import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { getCareerMaterials, setDefaultCareerMaterial } from "../api/careerMaterialsApi";
import CareerMaterialsPage from "./CareerMaterialsPage";

vi.mock("../api/careerMaterialsApi", () => ({
  getCareerMaterials: vi.fn(), uploadCareerMaterial: vi.fn(),
  setDefaultCareerMaterial: vi.fn(), deactivateCareerMaterial: vi.fn(),
  deleteCareerMaterial: vi.fn(),
  careerMaterialDownloadUrl: (id: number) => `/api/applicant-profile/materials/${id}/download`,
}));

const material = {
  id: 4, applicantProfileId: 1, materialType: "RESUME" as const,
  displayName: "Operations Resume", originalFilename: "operations.pdf",
  mimeType: "application/pdf", fileSize: 2048, active: true, notes: "Reviewed",
  targetJobFamily: "Operations", targetSeniority: "MID", versionLabel: "2026",
  profileDefault: false, createdAt: "2026-01-01T00:00:00Z",
  updatedAt: "2026-01-01T00:00:00Z",
};

describe("CareerMaterialsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(getCareerMaterials).mockResolvedValue([material]);
    vi.mocked(setDefaultCareerMaterial).mockResolvedValue({ ...material, profileDefault: true });
  });

  it("loads the canonical full-management surface", async () => {
    const user = userEvent.setup();
    render(<CareerMaterialsPage />);
    expect(screen.getByText("Loading career materials...")).toBeInTheDocument();
    expect(await screen.findByRole("heading", { name: "Career Materials" })).toBeInTheDocument();
    expect(screen.getByText("Operations Resume")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Download" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Set as default" }));
    expect(setDefaultCareerMaterial).toHaveBeenCalledWith(4);
  });

  it("reports load failures without navigating away", async () => {
    vi.mocked(getCareerMaterials).mockRejectedValue(new Error("Materials unavailable"));
    render(<CareerMaterialsPage />);
    expect(await screen.findByRole("alert")).toHaveTextContent("Materials unavailable");
  });
});
