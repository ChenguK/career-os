import type {
  ApplicationPreparation,
  FormIdentity,
  ObservedQuestion,
  ApprovedFieldPlan,
  FieldPreparationResult,
  ReviewScreenshotReference,
} from "../domain.js";

export class CareerOsClient {
  private readonly baseUrl: string;

  constructor(baseUrl: string, private readonly request: typeof fetch = fetch) {
    this.baseUrl = baseUrl.replace(/\/$/, "");
  }

  getPreparation(applicationId: number): Promise<ApplicationPreparation> {
    return this.call(`/api/applications/${applicationId}/preparation`);
  }

  markOpening(applicationId: number, sessionId: number) {
    return this.action(applicationId, sessionId, "opening");
  }

  markCollectingQuestions(applicationId: number, sessionId: number) {
    return this.action(applicationId, sessionId, "collecting-questions");
  }

  recordObservations(applicationId: number, sessionId: number,
    identity: FormIdentity, questions: ObservedQuestion[]) {
    return this.call(
      `/api/applications/${applicationId}/preparation/sessions/${sessionId}/observations`,
      { method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identity, questions }) },
    );
  }

  markFailed(applicationId: number, sessionId: number,
    safeUserMessage: string, retryable: boolean) {
    return this.call(
      `/api/applications/${applicationId}/preparation/sessions/${sessionId}/failed`,
      { method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ safeUserMessage, retryable }) },
    );
  }

  createFieldPlan(applicationId: number, sessionId: number): Promise<ApprovedFieldPlan> {
    return this.call(`/api/applications/${applicationId}/preparation/sessions/${sessionId}/field-plan`,
      { method: "POST" }) as Promise<ApprovedFieldPlan>;
  }

  recordFieldResults(applicationId: number, sessionId: number,
    results: FieldPreparationResult[]) {
    return this.call(`/api/applications/${applicationId}/preparation/sessions/${sessionId}/field-results`,
      { method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ results }) });
  }

  pause(applicationId: number, sessionId: number, checkpoint: {
    currentPage: string | null; currentQuestion: string | null;
    checkpoint: string; snapshotHash: string | null;
  }) {
    return this.call(`/api/applications/${applicationId}/preparation/sessions/${sessionId}/pause`,
      { method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify(checkpoint) });
  }

  createReview(applicationId: number, sessionId: number,
    screenshots: ReviewScreenshotReference[]) {
    return this.call(`/api/applications/${applicationId}/preparation/sessions/${sessionId}/review`,
      { method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ screenshots }) });
  }

  private action(applicationId: number, sessionId: number, action: string) {
    return this.call(
      `/api/applications/${applicationId}/preparation/sessions/${sessionId}/${action}`,
      { method: "POST" },
    );
  }

  private async call(path: string, init?: RequestInit) {
    const response = await this.request(`${this.baseUrl}${path}`, init);
    if (!response.ok) {
      let message = `CareerOS backend request failed (${response.status})`;
      try {
        const body = await response.json() as { message?: string };
        if (body.message) message = body.message;
      } catch { /* Keep the safe status-only message. */ }
      throw new Error(message);
    }
    return response.status === 204 ? undefined : response.json();
  }
}
