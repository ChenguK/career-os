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
    expect(await screen.findByText(/1 blockers/i)).toBeInTheDocument();
    await user.type(screen.getByLabelText("Answer"), "Because it fits");
    await user.click(screen.getByRole("button", { name: /save answer/i }));
    expect(api.answerQuestion).toHaveBeenCalledWith(1, "Because it fits");
    expect(screen.getByRole("link", { name: /back to application tracker/i }))
      .toHaveAttribute("href", "/applications?editJob=12");
  });
});
