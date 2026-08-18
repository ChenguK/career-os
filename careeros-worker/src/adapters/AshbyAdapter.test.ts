import type { Page } from "playwright";
import { describe, expect, it, vi } from "vitest";

import { AshbyAdapter } from "./AshbyAdapter.js";

describe("AshbyAdapter", () => {
  const adapter = new AshbyAdapter();

  it("detects only public Ashby job URLs", () => {
    expect(adapter.canHandle(new URL("https://jobs.ashbyhq.com/acme/job-123")))
      .toBe(true);
    expect(adapter.canHandle(new URL("http://jobs.ashbyhq.com/acme/job-123")))
      .toBe(false);
    expect(adapter.canHandle(new URL("https://example.com/acme/job-123")))
      .toBe(false);
  });

  it("inspects deterministic Ashby form identity", async () => {
    const page = {
      url: () => "https://jobs.ashbyhq.com/acme/job-123#application",
    } as unknown as Page;

    await expect(adapter.inspectIdentity(page)).resolves.toEqual({
      normalizedFormUrl: "https://jobs.ashbyhq.com/acme/job-123",
      externalRequisitionId: "job-123",
      externalFormKey: "ashby:acme:job-123",
    });
  });

  it("exposes the empty question collection contract without inspecting a form", async () => {
    const evaluate = vi.fn();
    const page = { evaluate } as unknown as Page;

    await expect(adapter.collectQuestions(page)).resolves.toEqual([]);
    expect(evaluate).not.toHaveBeenCalled();
  });

  it("prepares exact approved fields while auditing skips and safe failures", async () => {
    const fill = vi.fn().mockResolvedValue(undefined);
    const check = vi.fn().mockRejectedValue(new Error("private browser detail"));
    const locator = vi.fn((selector: string) => selector.includes("missing")
      ? { count: async () => 0 } : selector.includes("willing")
        ? { count: async () => 1, check, uncheck: vi.fn() }
        : { count: async () => 1, fill });
    const page = { locator } as unknown as Page;
    const base = { source: "APPLICANT_PROFILE" as const, sourceRecordId: 1,
      sourceVerifiedAt: "2026-08-18T10:00:00Z" };
    const results = await adapter.prepareFields(page, [
      { ...base, id: 1, canonicalKey: "first_name", answerType: "TEXT",
        textValue: "Ada", booleanValue: null, numberValue: null },
      { ...base, id: 2, canonicalKey: "missing", answerType: "TEXT",
        textValue: "x", booleanValue: null, numberValue: null },
      { ...base, id: 3, canonicalKey: "willing_to_relocate", answerType: "BOOLEAN",
        textValue: null, booleanValue: true, numberValue: null },
    ]);
    expect(fill).toHaveBeenCalledWith("Ada");
    expect(results.map(r => r.outcome)).toEqual(["PREPARED","SKIPPED","FAILED"]);
    expect(results[2]?.safeMessage).not.toContain("private browser detail");
  });
});
