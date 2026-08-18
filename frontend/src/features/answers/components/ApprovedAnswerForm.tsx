import { useState } from "react";

import type {
  AnswerClassification,
  AnswerSource,
  AnswerType,
  ApprovedAnswerInput,
  ProfileAnswerField,
} from "../types/approvedAnswer";

interface Props {
  initialValues: ApprovedAnswerInput;
  isEditing: boolean;
  onSubmit: (input: ApprovedAnswerInput) => Promise<void>;
  onCancel?: () => void;
}

const profileMappings: Record<string, {
  question: string;
  type: AnswerType;
  field: ProfileAnswerField;
}> = {
  willing_to_relocate: {
    question: "Are you willing to relocate?",
    type: "BOOLEAN",
    field: "WILLING_TO_RELOCATE",
  },
  willing_to_travel: {
    question: "Are you willing to travel?",
    type: "BOOLEAN",
    field: "WILLING_TO_TRAVEL",
  },
  salary_expectation: {
    question: "What is your minimum salary expectation?",
    type: "NUMBER",
    field: "MINIMUM_SALARY",
  },
};

export default function ApprovedAnswerForm({
  initialValues, isEditing, onSubmit, onCancel,
}: Props) {
  const [values, setValues] = useState(initialValues);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");

  function set<K extends keyof ApprovedAnswerInput>(
    key: K, value: ApprovedAnswerInput[K],
  ) {
    setValues((current) => ({ ...current, [key]: value }));
  }

  function changeSource(source: AnswerSource) {
    if (source === "APPLICANT_PROFILE") {
      const mapping = profileMappings.willing_to_relocate;
      setValues((current) => ({
        ...current,
        answerSource: source,
        canonicalKey: "willing_to_relocate",
        representativeQuestion: mapping.question,
        answerType: mapping.type,
        profileField: mapping.field,
        textValue: null,
        booleanValue: null,
        numberValue: null,
      }));
      return;
    }
    setValues((current) => ({
      ...current,
      answerSource: source,
      canonicalKey: "",
      representativeQuestion: "",
      answerType: "TEXT",
      profileField: null,
      textValue: "",
      booleanValue: null,
      numberValue: null,
    }));
  }

  function changeProfileMapping(key: string) {
    const mapping = profileMappings[key];
    setValues((current) => ({
      ...current,
      canonicalKey: key,
      representativeQuestion: mapping.question,
      answerType: mapping.type,
      profileField: mapping.field,
    }));
  }

  function changeType(type: AnswerType) {
    setValues((current) => ({
      ...current,
      answerType: type,
      textValue: type === "TEXT" ? "" : null,
      booleanValue: type === "BOOLEAN" ? false : null,
      numberValue: type === "NUMBER" ? 0 : null,
    }));
  }

  function changeClassification(classification: AnswerClassification) {
    setValues((current) => ({
      ...current,
      classification,
      reusable: classification === "VERIFIED_REUSABLE"
        ? current.reusable
        : false,
    }));
  }

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setError("");
    try {
      await onSubmit(values);
    } catch (caughtError) {
      setError(caughtError instanceof Error
        ? caughtError.message
        : "Approved answer could not be saved.");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="approved-answer-form" onSubmit={(event) => void submit(event)}>
      <h2>{isEditing ? "Edit approved answer" : "Add approved answer"}</h2>
      <p>Saving does not approve an answer. Approval is always a separate action.</p>
      {error && <p role="alert">{error}</p>}

      <label>Source
        <select value={values.answerSource}
          onChange={(event) => changeSource(event.target.value as AnswerSource)}>
          <option value="MANUAL">Manual</option>
          <option value="APPLICANT_PROFILE">Applicant Profile</option>
        </select>
      </label>

      {values.answerSource === "APPLICANT_PROFILE" ? (
        <label>Profile-backed concept
          <select value={values.canonicalKey}
            onChange={(event) => changeProfileMapping(event.target.value)}>
            {Object.entries(profileMappings).map(([key, mapping]) => (
              <option key={key} value={key}>{mapping.question}</option>
            ))}
          </select>
        </label>
      ) : (
        <>
          <label>Canonical key
            <input required pattern="[A-Za-z][A-Za-z0-9_]{2,79}"
              value={values.canonicalKey}
              onChange={(event) => set("canonicalKey", event.target.value)} />
          </label>
          <label>Answer type
            <select value={values.answerType}
              onChange={(event) => changeType(event.target.value as AnswerType)}>
              <option value="TEXT">Text</option>
              <option value="BOOLEAN">Yes / No</option>
              <option value="NUMBER">Number</option>
            </select>
          </label>
        </>
      )}

      <label>Representative question
        <input required maxLength={500} value={values.representativeQuestion}
          onChange={(event) => set("representativeQuestion", event.target.value)} />
      </label>

      {values.answerSource === "MANUAL" && values.answerType === "TEXT" && (
        <label>Answer
          <textarea required value={values.textValue ?? ""}
            onChange={(event) => set("textValue", event.target.value)} />
        </label>
      )}
      {values.answerSource === "MANUAL" && values.answerType === "BOOLEAN" && (
        <label>Answer
          <select value={String(values.booleanValue)}
            onChange={(event) => set("booleanValue", event.target.value === "true")}>
            <option value="true">Yes</option>
            <option value="false">No</option>
          </select>
        </label>
      )}
      {values.answerSource === "MANUAL" && values.answerType === "NUMBER" && (
        <label>Answer
          <input required type="number" step="0.01" value={values.numberValue ?? ""}
            onChange={(event) => set("numberValue", event.target.value === ""
              ? null : Number(event.target.value))} />
        </label>
      )}

      <label>Classification
        <select value={values.classification}
          onChange={(event) => changeClassification(
            event.target.value as AnswerClassification,
          )}>
          <option value="VERIFIED_REUSABLE">Verified reusable</option>
          <option value="CONTEXTUAL">Contextual — review each time</option>
          <option value="SENSITIVE">Sensitive — explicit confirmation</option>
          <option value="UNKNOWN">Unknown — not approved</option>
        </select>
      </label>
      <label className="checkbox-label">
        <input type="checkbox" checked={values.reusable}
          disabled={values.classification !== "VERIFIED_REUSABLE"}
          onChange={(event) => set("reusable", event.target.checked)} />
        Mark reusable after approval
      </label>
      <label>Notes
        <textarea value={values.notes ?? ""}
          onChange={(event) => set("notes", event.target.value || null)} />
      </label>
      <div className="approved-answer-actions">
        <button type="submit" disabled={isSaving}>
          {isSaving ? "Saving…" : isEditing ? "Save changes" : "Add answer"}
        </button>
        {onCancel && <button type="button" onClick={onCancel}>Cancel</button>}
      </div>
    </form>
  );
}
