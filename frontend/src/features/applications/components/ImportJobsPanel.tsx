import { useState } from "react";

import {
  persistCsvImport,
  previewCsvImport,
} from "../api/applicationsApi";
import type {
  CanonicalImportRow,
  ImportPersistenceResponse,
  ImportPreviewResponse,
  ImportPreviewRow,
} from "../types/importPreview";
import ImportHistoryPanel from "./ImportHistoryPanel";

interface ImportJobsPanelProps {
  onClose: () => void;
  onImportComplete: () => Promise<void> | void;
}

const RESULT_LABELS = {
  CREATE: "Ready",
  REVIEW_WARNING: "Review",
  SKIP_DUPLICATE: "Already in CareerOS",
  INVALID: "Invalid",
} as const;

function displayEnum(value: string | null): string {
  if (!value) {
    return "—";
  }
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function displaySalary(row: ImportPreviewRow): string {
  const { salaryMin, salaryMax, salaryCurrency } = row.values;
  if (salaryMin == null && salaryMax == null) {
    return "—";
  }
  const currency = salaryCurrency ?? "USD";
  if (salaryMin != null && salaryMax != null) {
    return `${currency} ${salaryMin.toLocaleString()}–${salaryMax.toLocaleString()}`;
  }
  return `${currency} ${(salaryMin ?? salaryMax)?.toLocaleString()}`;
}

function issues(row: ImportPreviewRow): string[] {
  const all = [...row.errors, ...row.warnings];
  const visible = row.proposedAction === "SKIP_DUPLICATE"
    ? all.filter((issue) => issue.field !== "position_title"
      || !issue.message.toLowerCase().includes("match another job"))
    : all;
  return visible.map((issue) => issue.message);
}

function safeWebUrl(value: string | null): string | null {
  if (!value) {
    return null;
  }
  try {
    const url = new URL(value);
    return url.protocol === "http:" || url.protocol === "https:"
      ? value
      : null;
  } catch {
    return null;
  }
}

function canonicalFields(values: CanonicalImportRow): Record<string, string> {
  return Object.fromEntries(
    Object.entries(values)
      .filter(([, value]) => value !== null && value !== undefined)
      .map(([name, value]) => [
        name.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`),
        String(value),
      ]),
  );
}

export default function ImportJobsPanel({
  onClose,
  onImportComplete,
}: ImportJobsPanelProps) {
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<ImportPreviewResponse | null>(null);
  const [selectedRows, setSelectedRows] = useState<Set<number>>(new Set());
  const [isLoading, setIsLoading] = useState(false);
  const [isImporting, setIsImporting] = useState(false);
  const [error, setError] = useState("");
  const [batchResult, setBatchResult] =
    useState<ImportPersistenceResponse | null>(null);
  const [fileInputKey, setFileInputKey] = useState(0);
  const [showHistory, setShowHistory] = useState(false);

  async function handlePreview() {
    if (!file) {
      setError("Choose a CSV or XLSX file to preview.");
      return;
    }
    const lowerName = file.name.toLowerCase();
    if (!lowerName.endsWith(".csv") && !lowerName.endsWith(".xlsx")) {
      setError("Only CSV and XLSX files are supported.");
      return;
    }

    setIsLoading(true);
    setError("");
    try {
      const result = await previewCsvImport(file);
      setPreview(result);
      setBatchResult(null);
      setSelectedRows(new Set(
        result.rows
          .filter((row) => row.selectable)
          .map((row) => row.rowNumber),
      ));
    } catch (caughtError) {
      setPreview(null);
      setSelectedRows(new Set());
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "The import preview could not be generated.",
      );
    } finally {
      setIsLoading(false);
    }
  }

  async function handleImportSelected() {
    if (!preview || isImporting || batchResult) {
      return;
    }
    const rows = preview.rows
      .filter((row) => row.selectable && selectedRows.has(row.rowNumber))
      .map((row) => ({
        rowNumber: row.rowNumber,
        fields: canonicalFields(row.values),
      }));
    if (rows.length === 0) {
      setError("Select at least one eligible row to import.");
      return;
    }

    setIsImporting(true);
    setError("");
    try {
      const result = await persistCsvImport({
        filename: preview.filename,
        totalRows: preview.totalRows,
        rows,
      });
      setBatchResult(result);
      await onImportComplete();
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "The selected rows could not be imported.",
      );
    } finally {
      setIsImporting(false);
    }
  }

  function chooseAnotherFile() {
    setFile(null);
    setPreview(null);
    setBatchResult(null);
    setSelectedRows(new Set());
    setError("");
    setFileInputKey((current) => current + 1);
  }

  if (showHistory) {
    return (
      <section className="import-panel" role="dialog" aria-label="Import History">
        <div className="import-panel__header">
          <h2>Import History</h2>
          <button type="button" onClick={onClose} aria-label="Close import history">
            Close
          </button>
        </div>
        <ImportHistoryPanel onBackToImport={() => setShowHistory(false)} />
      </section>
    );
  }

  function toggleRow(rowNumber: number) {
    setSelectedRows((current) => {
      const next = new Set(current);
      if (next.has(rowNumber)) {
        next.delete(rowNumber);
      } else {
        next.add(rowNumber);
      }
      return next;
    });
  }

  return (
    <section className="import-panel" role="dialog" aria-labelledby="import-title">
      <div className="import-panel__header">
        <div>
          <h2 id="import-title">Import Jobs</h2>
          <p>Upload a CSV or Excel/XLSX file to preview it.</p>
        </div>
        <button type="button" onClick={onClose} aria-label="Close import preview">
          Close
        </button>
        <button type="button" onClick={() => setShowHistory(true)}>
          Import History
        </button>
      </div>

      <div className="import-panel__picker">
        <label>
          CSV or XLSX file
          <input
            key={fileInputKey}
            type="file"
            accept=".csv,.xlsx,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            onChange={(event) => {
              setFile(event.target.files?.[0] ?? null);
              setPreview(null);
              setBatchResult(null);
              setSelectedRows(new Set());
              setError("");
            }}
          />
        </label>
        <button type="button" disabled={!file || isLoading} onClick={handlePreview}>
          {isLoading ? "Generating preview…" : "Preview file"}
        </button>
      </div>

      {file && (
        <p>
          Selected: {file.name} ({file.name.toLowerCase().endsWith(".xlsx")
            ? "XLSX"
            : "CSV"})
        </p>
      )}

      {error && <p role="alert">{error}</p>}

      {preview && (
        <>
          <div className="import-summary" role="status">
            <span>{preview.totalRows} rows found</span>
            <span>{preview.createCount} Ready</span>
            <span>{preview.reviewCount} Review</span>
            <span>{preview.duplicateCount} Duplicate</span>
            <span>{preview.invalidCount} Invalid</span>
          </div>

          {preview.fileWarnings.map((warning) => (
            <p key={`${warning.field}-${warning.message}`} className="import-warning">
              {warning.message}: {warning.field}
            </p>
          ))}

          <div className="tracker-table-wrap">
            <table className="tracker-table import-preview-table">
              <thead>
                <tr>
                  <th>Row</th><th>Company</th><th>Position Title</th>
                  <th>Status</th><th>Priority</th><th>Match Score</th>
                  <th>Location</th><th>Work Arrangement</th><th>Salary</th>
                  <th>Application URL</th><th>Result</th><th>Issues</th>
                  <th>Select</th>
                </tr>
              </thead>
              <tbody>
                {preview.rows.map((row) => (
                  <tr key={row.rowNumber} className={`import-result--${row.proposedAction.toLowerCase()}`}>
                    <td>{row.rowNumber}</td>
                    <td>{row.values.companyName ?? "—"}</td>
                    <td>{row.values.positionTitle ?? "—"}</td>
                    <td>{displayEnum(row.values.status)}</td>
                    <td>{row.values.priority ?? "—"}</td>
                    <td>{row.values.matchScore ?? "—"}</td>
                    <td>{row.values.location ?? "—"}</td>
                    <td>{displayEnum(row.values.workArrangement)}</td>
                    <td>{displaySalary(row)}</td>
                    <td>
                      {safeWebUrl(row.values.applicationUrl) ? (
                        <a href={safeWebUrl(row.values.applicationUrl) ?? undefined} target="_blank" rel="noreferrer">
                          Open
                        </a>
                      ) : (row.values.applicationUrl ?? "—")}
                    </td>
                    <td>{RESULT_LABELS[row.proposedAction]}</td>
                    <td>
                      {issues(row).length > 0 ? (
                        <ul>{issues(row).map((issue) => <li key={issue}>{issue}</li>)}</ul>
                      ) : "—"}
                    </td>
                    <td>
                      <input
                        type="checkbox"
                        aria-label={`Select CSV row ${row.rowNumber}`}
                        checked={selectedRows.has(row.rowNumber)}
                        disabled={!row.selectable}
                        onChange={() => toggleRow(row.rowNumber)}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <button
            type="button"
            disabled={selectedRows.size === 0 || isImporting || Boolean(batchResult)}
            onClick={handleImportSelected}
          >
            {isImporting
              ? "Importing selected…"
              : `Import Selected (${selectedRows.size})`}
          </button>

          {batchResult && (
            <section className="import-complete" aria-labelledby="import-complete-title">
              <h3 id="import-complete-title">Import complete</h3>
              <p>Batch #{batchResult.batchId}</p>
              <div className="import-summary" role="status">
                <span>{batchResult.created} Created</span>
                <span>{batchResult.createdWithWarnings} Created with warnings</span>
                <span>{batchResult.skippedDuplicates} Skipped</span>
                <span>{batchResult.failed} Failed</span>
              </div>
              {batchResult.rows
                .filter((row) => row.status.startsWith("FAILED")
                  || row.status === "SKIPPED_DUPLICATE")
                .map((row) => (
                  <p key={row.rowNumber}>
                    Row {row.rowNumber}: {[...row.errors, ...row.warnings].join("; ")}
                  </p>
                ))}
              <button type="button" onClick={chooseAnotherFile}>
                Choose another file
              </button>
            </section>
          )}
        </>
      )}
    </section>
  );
}
