import { afterEach, describe, expect, it, vi } from "vitest";
import { approveAnswer, createApprovedAnswer, deleteApprovedAnswer,
  getApprovedAnswers, revokeAnswer, updateApprovedAnswer } from "./approvedAnswersApi";
import { emptyApprovedAnswerInput } from "../types/approvedAnswer";

describe("approvedAnswersApi", () => {
  afterEach(() => vi.restoreAllMocks());
  it("uses CRUD and separate approval endpoints", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockImplementation(
      async () => new Response(JSON.stringify([]), { status: 200,
        headers: { "Content-Type": "application/json" } }),
    );
    await getApprovedAnswers();
    await createApprovedAnswer(emptyApprovedAnswerInput);
    await updateApprovedAnswer(3, emptyApprovedAnswerInput);
    await approveAnswer(3);
    await revokeAnswer(3);
    fetchMock.mockResolvedValueOnce(new Response(null, { status: 204 }));
    await deleteApprovedAnswer(3);
    expect(fetchMock.mock.calls.map(([url, init]) => [url, init?.method])).toEqual([
      ["/api/approved-answers", undefined], ["/api/approved-answers", "POST"],
      ["/api/approved-answers/3", "PUT"],
      ["/api/approved-answers/3/approve", "POST"],
      ["/api/approved-answers/3/revoke", "POST"],
      ["/api/approved-answers/3", "DELETE"],
    ]);
  });
});
