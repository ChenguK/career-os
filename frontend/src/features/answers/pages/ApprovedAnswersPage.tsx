import { useEffect, useMemo, useState } from "react";

import {
  approveAnswer,
  createApprovedAnswer,
  deleteApprovedAnswer,
  getApprovedAnswers,
  revokeAnswer,
  updateApprovedAnswer,
} from "../api/approvedAnswersApi";
import ApprovedAnswerForm from "../components/ApprovedAnswerForm";
import type {
  ApprovedAnswer,
  ApprovedAnswerInput,
} from "../types/approvedAnswer";
import {
  answerToInput,
  emptyApprovedAnswerInput,
} from "../types/approvedAnswer";

function displayValue(answer: ApprovedAnswer): string {
  const text = answer.answerSource === "APPLICANT_PROFILE"
    ? answer.resolvedTextValue
    : answer.textValue;
  const bool = answer.answerSource === "APPLICANT_PROFILE"
    ? answer.resolvedBooleanValue
    : answer.booleanValue;
  const number = answer.answerSource === "APPLICANT_PROFILE"
    ? answer.resolvedNumberValue
    : answer.numberValue;
  if (text !== null) return text;
  if (bool !== null) return bool ? "Yes" : "No";
  if (number !== null) {
    return answer.resolvedCurrency
      ? `${number.toLocaleString()} ${answer.resolvedCurrency}`
      : number.toLocaleString();
  }
  return "Unknown / unanswered";
}

export default function ApprovedAnswersPage() {
  const [answers, setAnswers] = useState<ApprovedAnswer[]>([]);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let cancelled = false;
    getApprovedAnswers()
      .then((result) => { if (!cancelled) setAnswers(result); })
      .catch((caughtError: unknown) => {
        if (!cancelled) setError(caughtError instanceof Error
          ? caughtError.message : "Approved answers could not be loaded.");
      })
      .finally(() => { if (!cancelled) setIsLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const editing = useMemo(
    () => answers.find((answer) => answer.id === editingId) ?? null,
    [answers, editingId],
  );

  function replace(updated: ApprovedAnswer) {
    setAnswers((current) => current
      .map((answer) => answer.id === updated.id ? updated : answer)
      .sort((a, b) => a.canonicalKey.localeCompare(b.canonicalKey)));
  }

  async function save(input: ApprovedAnswerInput) {
    setError("");
    setMessage("");
    if (editing) {
      replace(await updateApprovedAnswer(editing.id, input));
      setEditingId(null);
      setMessage("Answer saved. Semantic changes require explicit re-approval.");
    } else {
      const created = await createApprovedAnswer(input);
      setAnswers((current) => [...current, created]
        .sort((a, b) => a.canonicalKey.localeCompare(b.canonicalKey)));
      setMessage("Answer saved. Approve it explicitly when ready.");
    }
  }

  async function act(id: number, action: "approve" | "revoke" | "delete") {
    setBusyId(id);
    setError("");
    setMessage("");
    try {
      if (action === "delete") {
        await deleteApprovedAnswer(id);
        setAnswers((current) => current.filter((answer) => answer.id !== id));
        if (editingId === id) setEditingId(null);
        setMessage("Approved answer deleted.");
      } else {
        const updated = action === "approve"
          ? await approveAnswer(id)
          : await revokeAnswer(id);
        replace(updated);
        setMessage(action === "approve"
          ? "Answer explicitly approved."
          : "Answer approval revoked.");
      }
    } catch (caughtError) {
      setError(caughtError instanceof Error
        ? caughtError.message : "The answer could not be updated.");
    } finally {
      setBusyId(null);
    }
  }

  if (isLoading) {
    return <main className="approved-answers-page"><p>Loading approved answers...</p></main>;
  }

  return (
    <main className="approved-answers-page">
      <header>
        <p>Career OS</p>
        <h1>Approved Answers</h1>
        <p>
          Remember answers you have explicitly reviewed. CareerOS does not use
          these answers in applications automatically.
        </p>
      </header>
      {error && <p role="alert">{error}</p>}
      {message && <p role="status">{message}</p>}

      <ApprovedAnswerForm
        key={editing?.id ?? "new"}
        initialValues={editing ? answerToInput(editing) : emptyApprovedAnswerInput}
        isEditing={editing !== null}
        onSubmit={save}
        onCancel={editing ? () => setEditingId(null) : undefined}
      />

      <section aria-labelledby="saved-answers-heading">
        <h2 id="saved-answers-heading">Saved answers</h2>
        {answers.length === 0 ? (
          <p>No approved answers have been saved yet.</p>
        ) : (
          <div className="approved-answer-list">
            {answers.map((answer) => (
              <article key={answer.id}>
                <div className="approved-answer-card__header">
                  <div>
                    <h3>{answer.representativeQuestion}</h3>
                    <code>{answer.canonicalKey}</code>
                  </div>
                  <strong>{answer.classification.replaceAll("_", " ")}</strong>
                </div>
                <p><strong>Answer:</strong> {displayValue(answer)}</p>
                <p>
                  {answer.userApproved ? "User approved" : "Not approved"}
                  {answer.effectiveReusable && " · Ready for future reuse"}
                </p>
                {answer.answerSource === "APPLICANT_PROFILE" && !answer.authorityAvailable && (
                  <p className="answer-authority-warning">
                    The Applicant Profile is unverified or this value is unknown.
                    This answer is not safe for reuse.
                  </p>
                )}
                {answer.notes && <p><strong>Notes:</strong> {answer.notes}</p>}
                <div className="approved-answer-actions">
                  <button type="button" onClick={() => setEditingId(answer.id)}
                    disabled={busyId !== null}>Edit</button>
                  {answer.userApproved ? (
                    <button type="button" onClick={() => void act(answer.id, "revoke")}
                      disabled={busyId !== null}>Revoke approval</button>
                  ) : (
                    <button type="button" onClick={() => void act(answer.id, "approve")}
                      disabled={busyId !== null || answer.classification === "UNKNOWN"
                        || !answer.authorityAvailable}>Approve</button>
                  )}
                  <button type="button" onClick={() => void act(answer.id, "delete")}
                    disabled={busyId !== null}>Delete</button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
