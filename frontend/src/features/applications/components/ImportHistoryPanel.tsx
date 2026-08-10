import { useEffect, useState } from "react";

import {
  getImportBatch,
  getImportHistory,
} from "../api/applicationsApi";
import type {
  ImportBatchDetail,
  ImportBatchSummary,
  ImportHistoryPage,
  ImportRowOutcomeStatus,
} from "../types/importPreview";

interface ImportHistoryPanelProps {
  onBackToImport: () => void;
}

const OUTCOME_LABELS: Record<ImportRowOutcomeStatus, string> = {
  CREATED: "Created",
  CREATED_WITH_WARNING: "Created with warning",
  SKIPPED_DUPLICATE: "Skipped duplicate",
  FAILED_VALIDATION: "Failed validation",
  FAILED_PERSISTENCE: "Failed persistence",
};

function completedAt(batch: ImportBatchSummary): string {
  return batch.completedAt
    ? new Date(batch.completedAt).toLocaleString()
    : "Not completed";
}

export default function ImportHistoryPanel({
  onBackToImport,
}: ImportHistoryPanelProps) {
  const [history, setHistory] =
    useState<ImportHistoryPage<ImportBatchSummary> | null>(null);
  const [historyPage, setHistoryPage] = useState(0);
  const [detail, setDetail] = useState<ImportBatchDetail | null>(null);
  const [detailPage, setDetailPage] = useState(0);
  const [selectedBatchId, setSelectedBatchId] = useState<number | null>(null);
  const [isHistoryLoading, setIsHistoryLoading] = useState(true);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [historyError, setHistoryError] = useState("");
  const [detailError, setDetailError] = useState("");

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setIsHistoryLoading(true);
      setHistoryError("");
      try {
        const result = await getImportHistory(historyPage, 25);
        if (!cancelled) setHistory(result);
      } catch (error) {
        if (!cancelled) {
          setHistoryError(error instanceof Error
            ? error.message : "Import history could not be loaded.");
        }
      } finally {
        if (!cancelled) setIsHistoryLoading(false);
      }
    }
    void load();
    return () => { cancelled = true; };
  }, [historyPage]);

  async function openBatch(batchId: number, page = 0) {
    setSelectedBatchId(batchId);
    setDetailPage(page);
    setIsDetailLoading(true);
    setDetailError("");
    try {
      setDetail(await getImportBatch(batchId, page, 25));
    } catch (error) {
      setDetailError(error instanceof Error
        ? error.message : "Import batch details could not be loaded.");
    } finally {
      setIsDetailLoading(false);
    }
  }

  if (detail) {
    return (
      <section aria-labelledby="import-detail-title">
        <button type="button" onClick={() => {
          setDetail(null);
          setSelectedBatchId(null);
          setDetailError("");
        }}>
          Back to Import History
        </button>
        <h3 id="import-detail-title">{detail.batch.filename}</h3>
        <p>Batch #{detail.batch.batchId} · {completedAt(detail.batch)}</p>
        <div className="tracker-table-wrap">
          <table className="tracker-table">
            <thead><tr>
              <th>CSV Row</th><th>Outcome</th><th>Identifiers</th><th>Issues</th>
            </tr></thead>
            <tbody>{detail.rows.content.map((row) => (
              <tr key={row.rowNumber}>
                <td>{row.rowNumber}</td>
                <td>{OUTCOME_LABELS[row.outcome]}</td>
                <td>
                  {row.companyId ? `Company #${row.companyId} ` : ""}
                  {row.jobOpportunityId ? `Job #${row.jobOpportunityId} ` : ""}
                  {row.applicationId ? `Application #${row.applicationId}` : ""}
                  {row.duplicateJobOpportunityId
                    ? `Duplicate Job #${row.duplicateJobOpportunityId}` : ""}
                  {!row.companyId && !row.jobOpportunityId
                    && !row.applicationId && !row.duplicateJobOpportunityId ? "—" : ""}
                </td>
                <td>{[...row.warnings, ...row.errors].join("; ") || "—"}</td>
              </tr>
            ))}</tbody>
          </table>
        </div>
        <nav className="tracker-pagination" aria-label="Import batch row pagination">
          <button type="button" disabled={detailPage === 0 || isDetailLoading}
            onClick={() => void openBatch(detail.batch.batchId, detailPage - 1)}>
            Previous
          </button>
          <span>Page {detail.rows.page + 1} of {Math.max(1, detail.rows.totalPages)}</span>
          <button type="button"
            disabled={detailPage + 1 >= detail.rows.totalPages || isDetailLoading}
            onClick={() => void openBatch(detail.batch.batchId, detailPage + 1)}>
            Next
          </button>
        </nav>
      </section>
    );
  }

  return (
    <section aria-labelledby="import-history-title">
      <button type="button" onClick={onBackToImport}>Back to Import Jobs</button>
      <h3 id="import-history-title">Import History</h3>
      {isHistoryLoading && <p>Loading import history…</p>}
      {historyError && <p role="alert">{historyError}</p>}
      {!isHistoryLoading && !historyError && history?.content.length === 0 && (
        <p>No imports yet. Completed CSV imports will appear here.</p>
      )}
      {!isHistoryLoading && history && history.content.length > 0 && (
        <>
          <div className="import-history-list">
            {history.content.map((batch) => (
              <article key={batch.batchId}>
                <h4>{batch.filename}</h4>
                <p>{completedAt(batch)}</p>
                <p>
                  {batch.created} Created · {batch.createdWithWarnings} Warnings ·{" "}
                  {batch.skippedDuplicates} Skipped · {batch.failed} Failed
                </p>
                <button type="button" onClick={() => void openBatch(batch.batchId)}>
                  View batch #{batch.batchId}
                </button>
              </article>
            ))}
          </div>
          <nav className="tracker-pagination" aria-label="Import history pagination">
            <button type="button" disabled={history.page === 0}
              onClick={() => setHistoryPage((page) => page - 1)}>Previous</button>
            <span>Page {history.page + 1} of {Math.max(1, history.totalPages)}</span>
            <button type="button" disabled={history.page + 1 >= history.totalPages}
              onClick={() => setHistoryPage((page) => page + 1)}>Next</button>
          </nav>
        </>
      )}
      {isDetailLoading && <p>Loading batch details…</p>}
      {detailError && selectedBatchId !== null && <p role="alert">{detailError}</p>}
    </section>
  );
}
