import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import ApprovedAnswersPage from "./ApprovedAnswersPage";
import * as api from "../api/approvedAnswersApi";
import type { ApprovedAnswer } from "../types/approvedAnswer";

vi.mock("../api/approvedAnswersApi");
function answer(overrides: Partial<ApprovedAnswer> = {}): ApprovedAnswer {
  return { id: 1, canonicalKey: "preferred_pronouns",
    representativeQuestion: "What are your pronouns?", answerType: "TEXT",
    textValue: "they/them", booleanValue: null, numberValue: null,
    classification: "VERIFIED_REUSABLE", reusable: true, userApproved: false,
    approvedAt: null, lastUsedAt: null, answerSource: "MANUAL", profileField: null,
    authorityAvailable: true, effectiveReusable: false,
    resolvedTextValue: "they/them", resolvedBooleanValue: null,
    resolvedNumberValue: null, resolvedCurrency: null, notes: null,
    createdAt: "2026-08-18T12:00:00Z", updatedAt: "2026-08-18T12:00:00Z",
    ...overrides };
}

describe("ApprovedAnswersPage", () => {
  beforeEach(() => vi.mocked(api.getApprovedAnswers).mockResolvedValue([]));
  it("loads and renders the empty state", async () => {
    render(<ApprovedAnswersPage />);
    expect(screen.getByText(/loading approved answers/i)).toBeInTheDocument();
    expect(await screen.findByText(/no approved answers have been saved/i)).toBeInTheDocument();
    expect(screen.getByText(/does not use these answers/i)).toBeInTheDocument();
  });
  it("adds a contextual manual answer without approval", async () => {
    const created = answer({ canonicalKey: "availability_context",
      classification: "CONTEXTUAL", reusable: false,
      representativeQuestion: "When can you start?", textValue: "Review per role",
      resolvedTextValue: "Review per role" });
    vi.mocked(api.createApprovedAnswer).mockResolvedValue(created);
    const user = userEvent.setup(); render(<ApprovedAnswersPage />);
    await screen.findByText(/no approved answers/i);
    await user.type(screen.getByLabelText(/canonical key/i), "availability_context");
    await user.type(screen.getByLabelText(/representative question/i), "When can you start?");
    await user.type(screen.getByLabelText(/^answer$/i), "Review per role");
    await user.selectOptions(screen.getByLabelText(/classification/i), "CONTEXTUAL");
    await user.click(screen.getByRole("button", { name: /add answer/i }));
    expect(api.createApprovedAnswer).toHaveBeenCalledWith(expect.objectContaining({
      canonicalKey: "availability_context", classification: "CONTEXTUAL", reusable: false }));
    expect(await screen.findByText("When can you start?")).toBeInTheDocument();
    expect(screen.getByText("Not approved")).toBeInTheDocument();
  });
  it("approves, revokes, edits, and deletes through explicit actions", async () => {
    const base = answer();
    vi.mocked(api.getApprovedAnswers).mockResolvedValue([base]);
    vi.mocked(api.approveAnswer).mockResolvedValue({ ...base, userApproved: true,
      effectiveReusable: true });
    vi.mocked(api.revokeAnswer).mockResolvedValue(base);
    vi.mocked(api.updateApprovedAnswer).mockResolvedValue({ ...base,
      representativeQuestion: "Which pronouns should we use?" });
    vi.mocked(api.deleteApprovedAnswer).mockResolvedValue();
    const user = userEvent.setup(); render(<ApprovedAnswersPage />);
    await user.click(await screen.findByRole("button", { name: "Approve" }));
    expect(api.approveAnswer).toHaveBeenCalledWith(1);
    await user.click(screen.getByRole("button", { name: /revoke approval/i }));
    await user.click(screen.getByRole("button", { name: "Edit" }));
    const question = screen.getByLabelText(/representative question/i);
    await user.clear(question); await user.type(question, "Which pronouns should we use?");
    await user.click(screen.getByRole("button", { name: /save changes/i }));
    expect(api.updateApprovedAnswer).toHaveBeenCalled();
    await user.click(screen.getByRole("button", { name: "Delete" }));
    expect(api.deleteApprovedAnswer).toHaveBeenCalledWith(1);
  });
  it("preserves unknown profile state and disables approval", async () => {
    vi.mocked(api.getApprovedAnswers).mockResolvedValue([answer({
      canonicalKey: "willing_to_relocate", textValue: null,
      resolvedTextValue: null, resolvedBooleanValue: null, answerType: "BOOLEAN",
      answerSource: "APPLICANT_PROFILE", profileField: "WILLING_TO_RELOCATE",
      authorityAvailable: false })]);
    render(<ApprovedAnswersPage />);
    expect(await screen.findByText("Unknown / unanswered")).toBeInTheDocument();
    expect(screen.getByText(/profile is unverified or this value is unknown/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Approve" })).toBeDisabled();
  });
  it("surfaces load errors", async () => {
    vi.mocked(api.getApprovedAnswers).mockRejectedValueOnce(new Error("Offline"));
    render(<ApprovedAnswersPage />);
    await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent("Offline"));
  });
});
