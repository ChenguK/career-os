import { describe, expect, it, vi } from "vitest";

import type { CareerOsClient } from "./backend/CareerOsClient.js";
import { isMainModule, startWorker } from "./index.js";

describe("worker startup", () => {
  it("recognizes an entry path containing spaces as the main module", () => {
    expect(isMainModule(
      "file:///Users/example/GA%20Study%20Guides/career-os/careeros-worker/dist/index.js",
      "/Users/example/GA Study Guides/career-os/careeros-worker/dist/index.js",
    )).toBe(true);
  });

  it("starts safely idle without an assigned application", async () => {
    await expect(startWorker({})).resolves.toBe("idle");
  });

  it("validates application identity before starting", async () => {
    await expect(startWorker({ CAREEROS_APPLICATION_ID: "invalid" }))
      .rejects.toThrow("positive integer");
  });

  it("runs an assigned application through injected worker dependencies", async () => {
    const client = {
      getPreparation: vi.fn().mockResolvedValue({
        capability: "INSPECTION",
        session: { id: 7, applicationId: 12, state: "INITIALIZED",
          normalizedFormUrl: "https://jobs.ashbyhq.com/acme/job-1" },
      }),
      markOpening: vi.fn(), markCollectingQuestions: vi.fn(),
      recordObservations: vi.fn(), markFailed: vi.fn(),
    } as unknown as CareerOsClient;
    const adapter = {
      provider: "ASHBY", canHandle: vi.fn().mockReturnValue(true),
      inspectIdentity: vi.fn(), collectQuestions: vi.fn().mockResolvedValue([]),
    };

    await expect(startWorker({ CAREEROS_APPLICATION_ID: "12" }, {
      client, adapters: [adapter],
    })).resolves.toBe("completed");
  });
});
