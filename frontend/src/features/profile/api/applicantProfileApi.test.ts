import { afterEach, describe, expect, it, vi } from "vitest";

import {
  getApplicantProfile,
  saveApplicantProfile,
  verifyApplicantProfile,
} from "./applicantProfileApi";
import { emptyApplicantProfileInput } from "../types/applicantProfile";

describe("applicantProfileApi", () => {
  afterEach(() => vi.unstubAllGlobals());

  it("loads the canonical current profile", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true, status: 200, json: async () => ({ exists: false }),
    });
    vi.stubGlobal("fetch", fetchMock);

    await getApplicantProfile();

    expect(fetchMock).toHaveBeenCalledWith("/api/applicant-profile", undefined);
  });

  it("saves profile values through singleton PUT semantics", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true, status: 200, json: async () => ({ exists: true }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const input = {
      ...emptyApplicantProfileInput,
      firstName: "Chengu",
      lastName: "Kargbo",
      email: "chengu@example.com",
    };

    await saveApplicantProfile(input);

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/applicant-profile",
      expect.objectContaining({ method: "PUT", body: JSON.stringify(input) }),
    );
  });

  it("uses a separate endpoint for explicit verification", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true, status: 200, json: async () => ({ verified: true }),
    });
    vi.stubGlobal("fetch", fetchMock);

    await verifyApplicantProfile();

    expect(fetchMock).toHaveBeenCalledWith(
      "/api/applicant-profile/verify",
      { method: "POST" },
    );
  });
});
