import { afterEach, describe, expect, it, vi } from "vitest";

import { CareerOsClient } from "./CareerOsClient.js";

describe("CareerOsClient", () => {
  afterEach(() => vi.restoreAllMocks());

  it("communicates only through scoped preparation APIs", async () => {
    const request = vi.fn().mockImplementation(async () => new Response(
      JSON.stringify({ capability: "INSPECTION", session: null }),
      { status: 200, headers: { "Content-Type": "application/json" } },
    ));
    const client = new CareerOsClient("http://127.0.0.1:8080/", request);

    await client.getPreparation(12);
    await client.markOpening(12, 7);
    await client.markCollectingQuestions(12, 7);
    await client.recordObservations(12, 7, {
      normalizedFormUrl: "https://jobs.ashbyhq.com/acme/job-1",
      externalRequisitionId: "job-1", externalFormKey: "ashby:acme:job-1",
    }, []);
    await client.markFailed(12, 7, "Inspection failed", true);
    await client.createFieldPlan(12, 7);
    await client.recordFieldResults(12, 7, [{ planItemId: 1,
      outcome: "SKIPPED", safeMessage: "Not present", preparedAt: null }]);
    await client.pause(12, 7, { currentPage: "application",
      currentQuestion: "email", checkpoint: "field:email", snapshotHash: null });
    await client.createReview(12, 7, [{ reference: "reviews/7/page-1.png",
      pageKey: "application", capturedAt: "2026-08-18T20:00:00Z" }]);

    expect(request.mock.calls.map(([url]) => url)).toEqual([
      "http://127.0.0.1:8080/api/applications/12/preparation",
      "http://127.0.0.1:8080/api/applications/12/preparation/sessions/7/opening",
      "http://127.0.0.1:8080/api/applications/12/preparation/sessions/7/collecting-questions",
      "http://127.0.0.1:8080/api/applications/12/preparation/sessions/7/observations",
      "http://127.0.0.1:8080/api/applications/12/preparation/sessions/7/failed",
      "http://127.0.0.1:8080/api/applications/12/preparation/sessions/7/field-plan",
      "http://127.0.0.1:8080/api/applications/12/preparation/sessions/7/field-results",
      "http://127.0.0.1:8080/api/applications/12/preparation/sessions/7/pause",
      "http://127.0.0.1:8080/api/applications/12/preparation/sessions/7/review",
    ]);
    expect(request.mock.calls[3][1]).toEqual(expect.objectContaining({
      method: "POST", body: JSON.stringify({
        identity: { normalizedFormUrl: "https://jobs.ashbyhq.com/acme/job-1",
          externalRequisitionId: "job-1", externalFormKey: "ashby:acme:job-1" },
        questions: [],
      }),
    }));
  });

  it("returns safe backend failures", async () => {
    const request = vi.fn().mockResolvedValue(new Response(
      JSON.stringify({ message: "Session is stale" }),
      { status: 409, headers: { "Content-Type": "application/json" } },
    ));
    await expect(new CareerOsClient("http://localhost", request)
      .getPreparation(1)).rejects.toThrow("Session is stale");
  });
});
