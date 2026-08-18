import { describe, expect, it, vi } from "vitest";

import type { AtsAdapter } from "./adapters/AtsAdapter.js";
import type { CareerOsClient } from "./backend/CareerOsClient.js";
import { runApplication } from "./worker.js";

describe("runApplication", () => {
  it("detects one supported adapter without opening a browser", async () => {
    const formUrl = "https://jobs.ashbyhq.com/acme/job-1";
    const adapter: AtsAdapter = {
      provider: "ASHBY",
      canHandle: vi.fn().mockReturnValue(true),
      inspectIdentity: vi.fn(), collectQuestions: vi.fn(),
      prepareFields: vi.fn(),
    };
    const client = {
      getPreparation: vi.fn().mockResolvedValue({
        capability: "INSPECTION",
        session: { id: 7, applicationId: 12, state: "INITIALIZED",
          normalizedFormUrl: formUrl },
      }),
      markOpening: vi.fn(),
      markCollectingQuestions: vi.fn(),
      recordObservations: vi.fn(),
      markFailed: vi.fn(),
    } as unknown as CareerOsClient;

    await expect(runApplication(12, { client, adapters: [adapter] }))
      .resolves.toBe("ASHBY");

    expect(adapter.canHandle).toHaveBeenCalledOnce();
    expect(adapter.inspectIdentity).not.toHaveBeenCalled();
    expect(adapter.collectQuestions).not.toHaveBeenCalled();
    expect(client.markOpening).not.toHaveBeenCalled();
    expect(client.recordObservations).not.toHaveBeenCalled();
  });

  it("prepares only backend-approved plan fields and records every outcome", async () => {
    const results = [{ planItemId: 1, outcome: "PREPARED" as const,
      safeMessage: null, preparedAt: "2026-08-18T20:00:00Z" }];
    const adapter: AtsAdapter = { provider: "ASHBY", canHandle: () => true,
      inspectIdentity: vi.fn(), collectQuestions: vi.fn(),
      prepareFields: vi.fn().mockResolvedValue(results) };
    const page = { goto: vi.fn(), url: () => "https://jobs.ashbyhq.com/acme/1" };
    const browser = { newPage: vi.fn().mockResolvedValue(page), close: vi.fn() };
    const plan = { id: 2, sessionId: 7, generatedAt: "2026-08-18T19:00:00Z",
      fields: [{ id: 1, canonicalKey: "first_name", answerType: "TEXT" as const,
        textValue: "Ada", booleanValue: null, numberValue: null,
        source: "APPLICANT_PROFILE" as const, sourceRecordId: 3,
        sourceVerifiedAt: "2026-08-18T18:00:00Z" }] };
    const client = { getPreparation: vi.fn().mockResolvedValue({ capability: "FIELD_PREPARATION",
      session: { id: 7, applicationId: 12, state: "PREPARING_FIELDS",
        normalizedFormUrl: "https://jobs.ashbyhq.com/acme/1" } }),
      createFieldPlan: vi.fn().mockResolvedValue(plan), recordFieldResults: vi.fn() } as unknown as CareerOsClient;

    await runApplication(12,{client,adapters:[adapter],launchBrowser:async()=>browser as never});
    expect(adapter.prepareFields).toHaveBeenCalledWith(page,plan.fields);
    expect(client.recordFieldResults).toHaveBeenCalledWith(12,7,results);
    expect(browser.close).toHaveBeenCalledOnce();
  });

  it("does not automatically resume a paused session", async () => {
    const adapter: AtsAdapter = { provider: "ASHBY", canHandle: () => true,
      inspectIdentity: vi.fn(), collectQuestions: vi.fn(), prepareFields: vi.fn() };
    const client = { getPreparation: vi.fn().mockResolvedValue({ capability: "FIELD_PREPARATION",
      session: { id: 7, applicationId: 12, state: "WAITING_FOR_USER",
        normalizedFormUrl: "https://jobs.ashbyhq.com/acme/1", checkpoint: "field:email" } }) } as unknown as CareerOsClient;
    await expect(runApplication(12,{client,adapters:[adapter]}))
      .rejects.toThrow("explicitly resumed");
    expect(adapter.prepareFields).not.toHaveBeenCalled();
  });
});
