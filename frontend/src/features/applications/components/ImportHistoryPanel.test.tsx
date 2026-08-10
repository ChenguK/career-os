import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import ImportHistoryPanel from "./ImportHistoryPanel";
import { getImportBatch, getImportHistory } from "../api/applicationsApi";
import type {
  ImportBatchDetail,
  ImportBatchSummary,
} from "../types/importPreview";

vi.mock("../api/applicationsApi", () => ({
  getImportBatch: vi.fn(),
  getImportHistory: vi.fn(),
}));

const batch: ImportBatchSummary = {
  batchId: 7,
  filename: "jobs.csv",
  format: "CSV",
  schemaVersion: "careeros_job_import_v1",
  createdAt: "2026-08-10T11:59:00Z",
  completedAt: "2026-08-10T12:00:00Z",
  totalRows: 5,
  selectedRows: 5,
  created: 1,
  createdWithWarnings: 1,
  skippedDuplicates: 1,
  failed: 2,
};

const history = {
  content: [batch], page: 0, size: 25, totalRows: 1, totalPages: 1,
};

const detail: ImportBatchDetail = {
  batch,
  rows: {
    content: [
      { rowNumber: 2, outcome: "CREATED", companyId: 1,
        jobOpportunityId: 2, applicationId: 3,
        duplicateJobOpportunityId: null, warnings: [], errors: [] },
      { rowNumber: 3, outcome: "CREATED_WITH_WARNING", companyId: 1,
        jobOpportunityId: 4, applicationId: 5,
        duplicateJobOpportunityId: null,
        warnings: ["Possible duplicate"], errors: [] },
      { rowNumber: 4, outcome: "SKIPPED_DUPLICATE", companyId: null,
        jobOpportunityId: null, applicationId: null,
        duplicateJobOpportunityId: 9,
        warnings: ["Application URL already exists"], errors: [] },
      { rowNumber: 5, outcome: "FAILED_VALIDATION", companyId: null,
        jobOpportunityId: null, applicationId: null,
        duplicateJobOpportunityId: null, warnings: [],
        errors: ["Invalid priority"] },
      { rowNumber: 6, outcome: "FAILED_PERSISTENCE", companyId: null,
        jobOpportunityId: null, applicationId: null,
        duplicateJobOpportunityId: null, warnings: [],
        errors: ["Row could not be persisted"] },
    ],
    page: 0, size: 25, totalRows: 5, totalPages: 1,
  },
};

describe("ImportHistoryPanel", () => {
  beforeEach(() => vi.clearAllMocks());

  it("shows loading then an empty history state", async () => {
    let resolve!: (value: typeof history) => void;
    vi.mocked(getImportHistory).mockReturnValue(new Promise((done) => {
      resolve = done;
    }));
    render(<ImportHistoryPanel onBackToImport={vi.fn()} />);
    expect(screen.getByText("Loading import history…")).toBeInTheDocument();
    resolve({ ...history, content: [], totalRows: 0, totalPages: 0 });
    expect(await screen.findByText(/No imports yet/)).toBeInTheDocument();
    expect(getImportHistory).toHaveBeenCalledTimes(1);
  });

  it("shows batch counts and returns to import", async () => {
    vi.mocked(getImportHistory).mockResolvedValue(history);
    const back = vi.fn();
    const user = userEvent.setup();
    render(<ImportHistoryPanel onBackToImport={back} />);
    expect(await screen.findByText("jobs.csv")).toBeInTheDocument();
    expect(screen.getByText("1 Created · 1 Warnings · 1 Skipped · 2 Failed"))
      .toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Back to Import Jobs" }));
    expect(back).toHaveBeenCalledTimes(1);
  });

  it("loads only the selected detail and renders every outcome and issue", async () => {
    vi.mocked(getImportHistory).mockResolvedValue(history);
    vi.mocked(getImportBatch).mockResolvedValue(detail);
    const user = userEvent.setup();
    render(<ImportHistoryPanel onBackToImport={vi.fn()} />);
    await user.click(await screen.findByRole("button", { name: "View batch #7" }));

    expect(await screen.findByText("Created with warning")).toBeInTheDocument();
    expect(screen.getByText("Skipped duplicate")).toBeInTheDocument();
    expect(screen.getByText("Failed validation")).toBeInTheDocument();
    expect(screen.getByText("Failed persistence")).toBeInTheDocument();
    expect(screen.getByText("Possible duplicate")).toBeInTheDocument();
    expect(screen.getByText("Invalid priority")).toBeInTheDocument();
    expect(getImportHistory).toHaveBeenCalledTimes(1);
    expect(getImportBatch).toHaveBeenCalledTimes(1);

    await user.click(screen.getByRole("button", { name: "Back to Import History" }));
    expect(screen.getByText("jobs.csv")).toBeInTheDocument();
    expect(getImportHistory).toHaveBeenCalledTimes(1);
  });

  it("keeps history visible when detail loading fails", async () => {
    vi.mocked(getImportHistory).mockResolvedValue(history);
    vi.mocked(getImportBatch).mockRejectedValue(new Error("Batch unavailable"));
    const user = userEvent.setup();
    render(<ImportHistoryPanel onBackToImport={vi.fn()} />);
    await user.click(await screen.findByRole("button", { name: "View batch #7" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Batch unavailable");
    expect(screen.getByText("jobs.csv")).toBeInTheDocument();
  });

  it("shows a history load failure", async () => {
    vi.mocked(getImportHistory).mockRejectedValue(new Error("History unavailable"));
    render(<ImportHistoryPanel onBackToImport={vi.fn()} />);
    expect(await screen.findByRole("alert")).toHaveTextContent("History unavailable");
    expect(getImportBatch).not.toHaveBeenCalled();
  });
});
