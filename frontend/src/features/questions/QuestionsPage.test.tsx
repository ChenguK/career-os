import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import QuestionsPage from "./QuestionsPage";
import * as api from "./questionsApi";
import { getApplications, getApplicationAutomation } from "../applications/api/applicationsApi";
import { getApprovedAnswers } from "../answers/api/approvedAnswersApi";

vi.mock("./questionsApi");
vi.mock("../applications/api/applicationsApi", () => ({ getApplications: vi.fn(), getApplicationAutomation: vi.fn() }));
vi.mock("../answers/api/approvedAnswersApi", () => ({ getApprovedAnswers: vi.fn() }));

const question = { id: 1, applicationId: 7, jobOpportunityId: 12,
  companyName: "GitHub", positionTitle: "Engineer", lifecycleStatus: "SAVED",
  canonicalQuestionKey: "why_this_role", questionText: "Why this role?",
  answerType: "TEXT", required: true, classification: "CONTEXTUAL",
  status: "UNANSWERED", proposedAnswer: null, approvedAnswer: null,
  approvedAnswerId: null, source: "MANUAL", notes: null };

describe("QuestionsPage", () => {
  beforeEach(() => {
    vi.mocked(api.getQuestions).mockReset();
    vi.mocked(api.getQuestionMappings).mockReset();
    vi.mocked(api.confirmQuestionMapping).mockReset();
    vi.mocked(api.revokeQuestionMapping).mockReset();
    vi.mocked(getApplications).mockReset();
    vi.mocked(getApplicationAutomation).mockReset();
    vi.mocked(getApprovedAnswers).mockReset();
    vi.mocked(api.getQuestions).mockResolvedValue([]);
    vi.mocked(getApplications).mockResolvedValue([{ id: 7, companyName: "GitHub",
      positionTitle: "Engineer" } as never]);
    vi.mocked(getApprovedAnswers).mockResolvedValue([]);
    vi.mocked(getApplicationAutomation).mockResolvedValue({ id: 1,
      applicationId: 7, state: "NEEDS_ANSWERS", submissionMode: "PREPARE_ONLY",
      atsType: "UNKNOWN", unresolvedRequiredCount: 2, needsReviewCount: 1,
      blockerCount: 0, blockReason: null, approvedForPrepAt: null,
      readyForReviewAt: null, approvedToSubmitAt: null,
      updatedAt: "2026-08-18T12:00:00Z" });
    vi.mocked(api.getQuestionMappings).mockResolvedValue({ applicationId: 7,
      snapshotId: 3, canonicalKeys: [], questions: [] });
  });
  it("renders navigation-safe empty state without submission controls", async () => {
    render(<MemoryRouter><QuestionsPage /></MemoryRouter>);
    expect(await screen.findByText(/no application questions yet/i)).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /submit application/i })).not.toBeInTheDocument();
  });
  it("previews templates and creates only selected questions", async () => {
    vi.mocked(api.getTemplates).mockResolvedValue([
      { id: 1, jobFamily: "SOFTWARE_ENGINEER", seniority: null,
        canonicalQuestionKey: "github_url", representativeQuestion: "GitHub URL?",
        answerType: "TEXT", classification: "VERIFIED_REUSABLE",
        requiredByDefault: false, common: true },
      { id: 2, jobFamily: "SOFTWARE_ENGINEER", seniority: null,
        canonicalQuestionKey: "why_this_role", representativeQuestion: "Why this role?",
        answerType: "TEXT", classification: "CONTEXTUAL",
        requiredByDefault: false, common: true },
    ]);
    vi.mocked(api.addTemplates).mockResolvedValue([]);
    const user = userEvent.setup(); render(<MemoryRouter><QuestionsPage /></MemoryRouter>);
    await screen.findByText(/no application questions/i);
    await screen.findByRole("option", { name: /github — engineer/i });
    await user.selectOptions(screen.getByLabelText(/^application/i), "7");
    await user.click(screen.getByRole("button", { name: /preview templates/i }));
    await user.click(await screen.findByLabelText("GitHub URL?"));
    await user.click(screen.getByRole("button", { name: /add selected questions/i }));
    expect(api.addTemplates).toHaveBeenCalledWith(7, [2]);
  });
  it("supports manual answers and blocker presentation", async () => {
    vi.mocked(api.getQuestions).mockResolvedValue([{ ...question, status: "BLOCKED" }]);
    vi.mocked(api.answerQuestion).mockResolvedValue({ ...question, status: "ANSWERED",
      proposedAnswer: "Because it fits" });
    const user = userEvent.setup(); render(<MemoryRouter><QuestionsPage /></MemoryRouter>);
    await screen.findByRole("option", { name: /github — engineer/i });
    expect(await screen.findByText(/1 blockers/i)).toBeInTheDocument();
    await user.type(screen.getByLabelText("Answer"), "Because it fits");
    await user.click(screen.getByRole("button", { name: /save answer/i }));
    expect(api.answerQuestion).toHaveBeenCalledWith(1, "Because it fits");
    expect(screen.getByRole("link", { name: /back to application tracker/i }))
      .toHaveAttribute("href", "/applications?editJob=12");
  });
  it("reviews and confirms an unmatched observed question without AI guessing language", async () => {
    vi.mocked(api.getQuestionMappings).mockResolvedValue({ applicationId: 7, snapshotId: 3,
      canonicalKeys: [{ canonicalQuestionKey: "github_url", representativeQuestion: "GitHub URL?",
        answerType: "TEXT", classification: "VERIFIED_REUSABLE", sources: ["APPLICANT_PROFILE"] }],
      questions: [{ mappingId: null, observedQuestionId: 8, externalQuestionId: "candidate-github",
        questionText: "Provide your GitHub profile", answerType: "TEXT", required: true,
        options: [{ id: 1, externalOptionId: "yes", value: "yes", label: "Yes", displayOrder: 0, active: true }],
        formIdentity: { normalizedFormUrl: "https://jobs.ashbyhq.com/acme/1",
          externalRequisitionId: "REQ-1", externalFormKey: "form-1" }, mappingState: "UNCONFIRMED",
        canonicalQuestionKey: null, mappingSource: null, mappingConfidence: null,
        userConfirmed: false, confirmedAt: null, revokedAt: null,
        suggestions: [{ canonicalKey: "github_url", representativeQuestion: "GitHub URL?",
          source: "EXACT_TEXT", confidence: 0.95,
          rationale: "Exact normalized wording; review is required." }] }] });
    vi.mocked(api.confirmQuestionMapping).mockResolvedValue({} as never);
    const user = userEvent.setup(); render(<MemoryRouter><QuestionsPage /></MemoryRouter>);
    await screen.findByRole("option", { name: /github — engineer/i });
    await user.selectOptions(await screen.findByLabelText(/^application/i), "7");
    expect(await screen.findByText(/provide your github profile/i)).toBeInTheDocument();
    expect(screen.getByText(/options: yes/i)).toBeInTheDocument();
    expect(screen.getByText(/form-1/i)).toBeInTheDocument();
    expect(screen.getByText(/deterministic candidate/i)).toBeInTheDocument();
    expect(screen.queryByText(/ai suggestion/i)).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /^confirm mapping$/i }));
    expect(api.confirmQuestionMapping).toHaveBeenCalledWith(
      7, "candidate-github", "github_url", "SOFTWARE_ENGINEER", "");
  });
  it("allows a confirmed mapping to be revoked", async () => {
    vi.mocked(api.getQuestionMappings).mockResolvedValue({ applicationId: 7, snapshotId: 3,
      canonicalKeys: [{ canonicalQuestionKey: "email", representativeQuestion: "Email",
        answerType: "TEXT", classification: "VERIFIED_REUSABLE", sources: ["APPLICANT_PROFILE"] }],
      questions: [{ mappingId: 9, observedQuestionId: 8, externalQuestionId: "email-field",
        questionText: "Email", answerType: "TEXT", required: true, options: [],
        formIdentity: { normalizedFormUrl: "https://jobs.ashbyhq.com/acme/1",
          externalRequisitionId: null, externalFormKey: "form-1" }, mappingState: "CONFIRMED",
        canonicalQuestionKey: "email", mappingSource: "USER", mappingConfidence: 1,
        userConfirmed: true, confirmedAt: "2026-08-18T12:00:00Z", revokedAt: null,
        suggestions: [] }] });
    vi.mocked(api.revokeQuestionMapping).mockResolvedValue({} as never);
    const user = userEvent.setup(); render(<MemoryRouter><QuestionsPage /></MemoryRouter>);
    await screen.findByRole("option", { name: /github — engineer/i });
    await user.selectOptions(await screen.findByLabelText(/^application/i), "7");
    await user.click(await screen.findByRole("button", { name: /revoke mapping/i }));
    expect(api.revokeQuestionMapping).toHaveBeenCalledWith(7, 9);
  });
});
