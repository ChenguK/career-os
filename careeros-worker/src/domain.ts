export type AnswerType = "TEXT" | "BOOLEAN" | "NUMBER" |
  "SINGLE_SELECT" | "MULTI_SELECT";

export interface FormIdentity {
  normalizedFormUrl: string;
  externalRequisitionId: string | null;
  externalFormKey: string | null;
}

export interface ObservedOption {
  externalOptionId: string | null;
  value: string;
  label: string;
  displayOrder: number;
}

export interface ObservedQuestion {
  externalQuestionId: string;
  questionText: string;
  answerType: AnswerType;
  required: boolean;
  displayOrder: number;
  options: ObservedOption[];
}

export interface PreparationSession {
  id: number;
  applicationId: number;
  state: "INITIALIZED" | "OPENING" | "COLLECTING_QUESTIONS" |
    "WAITING_FOR_USER" | "PREPARING_FIELDS" | "READY_FOR_REVIEW" |
    "FAILED" | "CANCELLED";
  normalizedFormUrl: string;
  currentPage?: string | null;
  currentQuestion?: string | null;
  checkpoint?: string | null;
  snapshotHash?: string | null;
  resumeState?: PreparationSession["state"] | null;
}

export interface ApplicationPreparation {
  capability: "NONE" | "SESSION_ONLY" | "INSPECTION" |
    "FIELD_PREPARATION" | "READY_FOR_REVIEW";
  session: PreparationSession | null;
}

export type FieldValueSource = "APPLICANT_PROFILE" | "APPROVED_ANSWER";
export interface ApprovedFieldPlanItem {
  id: number; canonicalKey: string; answerType: "TEXT" | "BOOLEAN" | "NUMBER";
  textValue: string | null; booleanValue: boolean | null; numberValue: number | null;
  source: FieldValueSource; sourceRecordId: number; sourceVerifiedAt: string;
}
export interface ApprovedFieldPlan {
  id: number; sessionId: number; generatedAt: string; fields: ApprovedFieldPlanItem[];
}
export type FieldPreparationOutcome = "PREPARED" | "SKIPPED" | "FAILED";
export interface FieldPreparationResult {
  planItemId: number; outcome: FieldPreparationOutcome;
  safeMessage: string | null; preparedAt: string | null;
}
export interface ReviewScreenshotReference {
  reference: string; pageKey: string | null; capturedAt: string;
}
