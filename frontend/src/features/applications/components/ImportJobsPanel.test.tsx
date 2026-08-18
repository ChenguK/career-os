import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import ImportJobsPanel from "./ImportJobsPanel";
import { persistCsvImport, previewCsvImport } from "../api/applicationsApi";
import type {
  ImportPreviewResponse,
  ImportPreviewRow,
  ImportProposedAction,
} from "../types/importPreview";

vi.mock("../api/applicationsApi", () => ({
  persistCsvImport: vi.fn(),
  previewCsvImport: vi.fn(),
}));

function row(
  rowNumber: number,
  proposedAction: ImportProposedAction,
  selectable: boolean,
  issue?: string,
): ImportPreviewRow {
  return {
    rowNumber,
    values: {
      positionTitle: `Engineer ${rowNumber}`,
      companyName: "Acme",
      location: "New York",
      workArrangement: "HYBRID",
      salaryMin: 100000,
      salaryMax: 120000,
      salaryCurrency: "USD",
      applicationUrl: "https://example.com/job",
      priority: 2,
      matchScore: 8.5,
      status: "APPLIED",
    },
    errors: proposedAction === "INVALID" && issue
      ? [{ field: "position_title", message: issue }]
      : [],
    warnings: proposedAction === "REVIEW_WARNING" && issue
      ? [{ field: "position_title", message: issue }]
      : proposedAction === "SKIP_DUPLICATE" && issue
        ? [{ field: "application_url", message: issue }]
        : [],
    normalizedApplicationUrl: "https://example.com/job",
    exactUrlDuplicate: null,
    companyTitleDuplicateCandidates: [],
    proposedAction,
    selectable,
  };
}

const response: ImportPreviewResponse = {
  filename: "jobs.csv",
  totalRows: 4,
  createCount: 1,
  reviewCount: 1,
  duplicateCount: 1,
  invalidCount: 1,
  hasFileErrors: false,
  hasFileWarnings: false,
  fileErrors: [],
  fileWarnings: [],
  rows: [
    row(2, "CREATE", true),
    row(3, "REVIEW_WARNING", true, "Possible duplicate company and title"),
    row(4, "SKIP_DUPLICATE", false, "Application URL already exists"),
    row(5, "INVALID", false, "Missing position title"),
  ],
};

async function chooseAndPreview(user: ReturnType<typeof userEvent.setup>) {
  const file = new File(["Job Title\nEngineer"], "jobs.csv", {
    type: "text/csv",
  });
  await user.upload(screen.getByLabelText("CSV or XLSX file"), file);
  await user.click(screen.getByRole("button", { name: "Preview file" }));
}

describe("ImportJobsPanel", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("accepts CSV and renders loading, summary, rows, and readable issues", async () => {
    let resolvePreview!: (value: ImportPreviewResponse) => void;
    vi.mocked(previewCsvImport).mockReturnValue(new Promise((resolve) => {
      resolvePreview = resolve;
    }));
    const user = userEvent.setup();
    render(<ImportJobsPanel onClose={vi.fn()} onImportComplete={vi.fn()} />);

    expect(screen.getByLabelText("CSV or XLSX file")).toHaveAttribute(
      "accept", ".csv,.xlsx,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    );
    await chooseAndPreview(user);
    expect(screen.getByRole("button", { name: "Generating preview…" }))
      .toBeDisabled();
    resolvePreview(response);

    expect(await screen.findByText("4 rows found")).toBeInTheDocument();
    expect(screen.getByText("1 Ready")).toBeInTheDocument();
    expect(screen.getByText("1 Review")).toBeInTheDocument();
    expect(screen.getByText("1 Duplicate")).toBeInTheDocument();
    expect(screen.getByText("1 Invalid")).toBeInTheDocument();
    expect(screen.getByText("Application URL already exists"))
      .toBeInTheDocument();
    expect(screen.getByText("Already in CareerOS")).toBeInTheDocument();
    expect(screen.getByText("Missing position title")).toBeInTheDocument();
    expect(previewCsvImport).toHaveBeenCalledWith(expect.any(File));
  });

  it("shows true unknown warnings without system-header warning noise", async () => {
    vi.mocked(previewCsvImport).mockResolvedValue({
      ...response,
      totalRows: 1,
      createCount: 1,
      reviewCount: 0,
      duplicateCount: 0,
      invalidCount: 0,
      hasFileWarnings: true,
      fileWarnings: [{
        field: "Mystery Column",
        message: "Unknown import header was ignored",
      }],
      rows: [row(2, "CREATE", true)],
    });
    const user = userEvent.setup();
    render(<ImportJobsPanel onClose={vi.fn()} onImportComplete={vi.fn()} />);
    await chooseAndPreview(user);

    expect(await screen.findByText(
      "Unknown import header was ignored: Mystery Column",
    )).toBeInTheDocument();
    expect(screen.queryByText(/job_id/)).not.toBeInTheDocument();
    expect(screen.getByText("1 Ready")).toBeInTheDocument();
  });

  it("selects ready and review rows and allows selectable rows to toggle", async () => {
    vi.mocked(previewCsvImport).mockResolvedValue(response);
    const user = userEvent.setup();
    render(<ImportJobsPanel onClose={vi.fn()} onImportComplete={vi.fn()} />);
    await chooseAndPreview(user);
    await screen.findByText("4 rows found");

    const ready = screen.getByLabelText("Select CSV row 2");
    const review = screen.getByLabelText("Select CSV row 3");
    const duplicate = screen.getByLabelText("Select CSV row 4");
    const invalid = screen.getByLabelText("Select CSV row 5");
    expect(ready).toBeChecked();
    expect(review).toBeChecked();
    expect(duplicate).not.toBeChecked();
    expect(duplicate).toBeDisabled();
    expect(invalid).not.toBeChecked();
    expect(invalid).toBeDisabled();

    await user.click(review);
    expect(review).not.toBeChecked();
    expect(screen.getByRole("button", {
      name: "Import Selected (1)",
    })).toBeEnabled();
  });

  it("shows validation errors and permits choosing another file", async () => {
    vi.mocked(previewCsvImport)
      .mockRejectedValueOnce(new Error("CSV file is empty"))
      .mockResolvedValueOnce(response);
    const user = userEvent.setup();
    render(<ImportJobsPanel onClose={vi.fn()} onImportComplete={vi.fn()} />);

    await chooseAndPreview(user);
    expect(await screen.findByRole("alert")).toHaveTextContent("CSV file is empty");

    const replacement = new File(["Job Title\nEngineer"], "replacement.csv", {
      type: "text/csv",
    });
    await user.upload(screen.getByLabelText("CSV or XLSX file"), replacement);
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Preview file" }));
    expect(await screen.findByText("4 rows found")).toBeInTheDocument();
  });

  it("accepts XLSX files", async () => {
    vi.mocked(previewCsvImport).mockResolvedValue(response);
    const user = userEvent.setup();
    render(<ImportJobsPanel onClose={vi.fn()} onImportComplete={vi.fn()} />);
    const input = screen.getByLabelText("CSV or XLSX file") as HTMLInputElement;
    const file = new File(["data"], "jobs.xlsx", {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    fireEvent.change(input, { target: { files: [file] } });
    await user.click(screen.getByRole("button", { name: "Preview file" }));

    expect(previewCsvImport).toHaveBeenCalledWith(file);
    expect(await screen.findByText("4 rows found")).toBeInTheDocument();
  });

  it("imports only selected eligible rows and completes the preview", async () => {
    vi.mocked(previewCsvImport).mockResolvedValue(response);
    vi.mocked(persistCsvImport).mockResolvedValue({
      batchId: 42,
      filename: "jobs.csv",
      totalRows: 4,
      selectedRows: 1,
      created: 1,
      createdWithWarnings: 0,
      skippedDuplicates: 0,
      failed: 0,
      rows: [{
        rowNumber: 2,
        status: "CREATED",
        companyId: 1,
        jobOpportunityId: 2,
        applicationId: 3,
        duplicateJobOpportunityId: null,
        warnings: [],
        errors: [],
      }],
    });
    const onImportComplete = vi.fn();
    const user = userEvent.setup();
    render(
      <ImportJobsPanel
        onClose={vi.fn()}
        onImportComplete={onImportComplete}
      />,
    );
    await chooseAndPreview(user);
    await screen.findByText("4 rows found");
    await user.click(screen.getByLabelText("Select CSV row 3"));
    await user.click(screen.getByRole("button", {
      name: "Import Selected (1)",
    }));

    await screen.findByText("Import complete");
    expect(persistCsvImport).toHaveBeenCalledWith(expect.objectContaining({
      filename: "jobs.csv",
      totalRows: 4,
      rows: [expect.objectContaining({
        rowNumber: 2,
        fields: expect.objectContaining({
          position_title: "Engineer 2",
          priority: "2",
        }),
      })],
    }));
    expect(onImportComplete).toHaveBeenCalledTimes(1);
    expect(screen.getByText("Batch #42")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Import Selected (1)" }))
      .toBeDisabled();
  });

  it("shows import loading, row failures, and choose-another-file", async () => {
    vi.mocked(previewCsvImport).mockResolvedValue(response);
    let resolveImport!: (value: Awaited<ReturnType<typeof persistCsvImport>>) => void;
    vi.mocked(persistCsvImport).mockReturnValue(new Promise((resolve) => {
      resolveImport = resolve;
    }));
    const user = userEvent.setup();
    render(<ImportJobsPanel onClose={vi.fn()} onImportComplete={vi.fn()} />);
    await chooseAndPreview(user);
    await screen.findByText("4 rows found");
    await user.click(screen.getByRole("button", { name: "Import Selected (2)" }));
    expect(screen.getByRole("button", { name: "Importing selected…" }))
      .toBeDisabled();

    resolveImport({
      batchId: 43,
      filename: "jobs.csv",
      totalRows: 4,
      selectedRows: 2,
      created: 0,
      createdWithWarnings: 0,
      skippedDuplicates: 1,
      failed: 1,
      rows: [
        {
          rowNumber: 2,
          status: "SKIPPED_DUPLICATE",
          companyId: null,
          jobOpportunityId: null,
          applicationId: null,
          duplicateJobOpportunityId: 9,
          warnings: ["Application URL already exists"],
          errors: [],
        },
        {
          rowNumber: 3,
          status: "FAILED_PERSISTENCE",
          companyId: null,
          jobOpportunityId: null,
          applicationId: null,
          duplicateJobOpportunityId: null,
          warnings: [],
          errors: ["Application could not be created"],
        },
      ],
    });

    expect(await screen.findByText("1 Skipped")).toBeInTheDocument();
    expect(screen.getByText(/Row 2: Application URL already exists/))
      .toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Choose another file" }));
    expect(screen.queryByText("Import complete")).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Preview file" })).toBeDisabled();
  });
});
