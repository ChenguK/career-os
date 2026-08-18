export type AnswerType = "TEXT" | "BOOLEAN" | "NUMBER";
export type AnswerClassification =
  | "VERIFIED_REUSABLE"
  | "CONTEXTUAL"
  | "SENSITIVE"
  | "UNKNOWN";
export type AnswerSource = "MANUAL" | "APPLICANT_PROFILE";
export type ProfileAnswerField =
  | "WILLING_TO_RELOCATE"
  | "WILLING_TO_TRAVEL"
  | "MINIMUM_SALARY";

export interface ApprovedAnswer {
  id: number;
  canonicalKey: string;
  representativeQuestion: string;
  answerType: AnswerType;
  textValue: string | null;
  booleanValue: boolean | null;
  numberValue: number | null;
  classification: AnswerClassification;
  reusable: boolean;
  userApproved: boolean;
  approvedAt: string | null;
  lastUsedAt: string | null;
  answerSource: AnswerSource;
  profileField: ProfileAnswerField | null;
  authorityAvailable: boolean;
  effectiveReusable: boolean;
  resolvedTextValue: string | null;
  resolvedBooleanValue: boolean | null;
  resolvedNumberValue: number | null;
  resolvedCurrency: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApprovedAnswerInput {
  canonicalKey: string;
  representativeQuestion: string;
  answerType: AnswerType;
  textValue: string | null;
  booleanValue: boolean | null;
  numberValue: number | null;
  classification: AnswerClassification;
  reusable: boolean;
  answerSource: AnswerSource;
  profileField: ProfileAnswerField | null;
  notes: string | null;
}

export const emptyApprovedAnswerInput: ApprovedAnswerInput = {
  canonicalKey: "",
  representativeQuestion: "",
  answerType: "TEXT",
  textValue: "",
  booleanValue: null,
  numberValue: null,
  classification: "UNKNOWN",
  reusable: false,
  answerSource: "MANUAL",
  profileField: null,
  notes: null,
};

export function answerToInput(answer: ApprovedAnswer): ApprovedAnswerInput {
  return {
    canonicalKey: answer.canonicalKey,
    representativeQuestion: answer.representativeQuestion,
    answerType: answer.answerType,
    textValue: answer.textValue,
    booleanValue: answer.booleanValue,
    numberValue: answer.numberValue,
    classification: answer.classification,
    reusable: answer.reusable,
    answerSource: answer.answerSource,
    profileField: answer.profileField,
    notes: answer.notes,
  };
}
