import { useState } from "react";

import type { ApplicationTrackerRow } from "../types/applicationTracker";

interface Props {
  row: ApplicationTrackerRow;
  onCancel: () => void;
  onConfirm: (applicationDate: string) => Promise<void>;
}

function localToday(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export default function ManualSubmissionDialog({ row, onCancel, onConfirm }: Props) {
  const [applicationDate, setApplicationDate] = useState(localToday);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await onConfirm(applicationDate);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "The submission could not be recorded.");
      setBusy(false);
    }
  }

  return <section className="manual-submission-dialog" role="dialog" aria-modal="true" aria-labelledby="manual-submission-heading">
    <form onSubmit={(event) => void submit(event)}>
      <h2 id="manual-submission-heading">Record application submission</h2>
      <p><strong>{row.companyName ?? "Unknown company"}</strong><br />{row.positionTitle}</p>
      <p>Use this when you submitted the application outside CareerOS.</p>
      <label>Date applied<input type="date" required max={localToday()} value={applicationDate} onChange={(event) => setApplicationDate(event.target.value)} /></label>
      {error && <p role="alert">{error}</p>}
      <div className="approved-answer-actions">
        <button type="button" disabled={busy} onClick={onCancel}>Cancel</button>
        <button type="submit" disabled={busy}>{busy ? "Recording…" : "Mark as Applied"}</button>
      </div>
    </form>
  </section>;
}
