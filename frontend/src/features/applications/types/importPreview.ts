import type { ApplicationStatus } from "./application";
import type { RemoteType } from "../../jobs/types/job";

export type ImportProposedAction =
  | "CREATE"
  | "REVIEW_WARNING"
  | "SKIP_DUPLICATE"
  | "INVALID";

export interface ImportIssue {
  field: string;
  message: string;
}

export interface ImportDuplicateMatch {
  jobOpportunityId: number | null;
  importRowNumber: number | null;
  companyName: string | null;
  positionTitle: string | null;
  applicationUrl: string | null;
}

export interface CanonicalImportRow {
  positionTitle: string | null;
  companyName: string | null;
  location: string | null;
  workArrangement: RemoteType | null;
  salaryMin: number | null;
  salaryMax: number | null;
  salaryCurrency: string | null;
  applicationUrl: string | null;
  priority: number | null;
  matchScore: number | null;
  status: ApplicationStatus | null;
  [field: string]: unknown;
}

export interface ImportPreviewRow {
  rowNumber: number;
  values: CanonicalImportRow;
  errors: ImportIssue[];
  warnings: ImportIssue[];
  normalizedApplicationUrl: string | null;
  exactUrlDuplicate: ImportDuplicateMatch | null;
  companyTitleDuplicateCandidates: ImportDuplicateMatch[];
  proposedAction: ImportProposedAction;
  selectable: boolean;
}

export interface ImportPreviewResponse {
  filename: string;
  totalRows: number;
  createCount: number;
  reviewCount: number;
  duplicateCount: number;
  invalidCount: number;
  hasFileErrors: boolean;
  hasFileWarnings: boolean;
  fileErrors: ImportIssue[];
  fileWarnings: ImportIssue[];
  rows: ImportPreviewRow[];
}

export type ImportRowOutcomeStatus =
  | "CREATED"
  | "CREATED_WITH_WARNING"
  | "SKIPPED_DUPLICATE"
  | "FAILED_VALIDATION"
  | "FAILED_PERSISTENCE";

export interface SelectedImportRowRequest {
  rowNumber: number;
  fields: Record<string, string>;
}

export interface ImportPersistenceRequest {
  filename: string;
  totalRows: number;
  rows: SelectedImportRowRequest[];
}

export interface ImportRowPersistenceResult {
  rowNumber: number;
  status: ImportRowOutcomeStatus;
  companyId: number | null;
  jobOpportunityId: number | null;
  applicationId: number | null;
  duplicateJobOpportunityId: number | null;
  warnings: string[];
  errors: string[];
}

export interface ImportPersistenceResponse {
  batchId: number;
  filename: string;
  totalRows: number;
  selectedRows: number;
  created: number;
  createdWithWarnings: number;
  skippedDuplicates: number;
  failed: number;
  rows: ImportRowPersistenceResult[];
}

export interface ImportHistoryPage<T> {
  content: T[];
  page: number;
  size: number;
  totalRows: number;
  totalPages: number;
}

export interface ImportBatchSummary {
  batchId: number;
  filename: string;
  format: string;
  schemaVersion: string;
  createdAt: string;
  completedAt: string | null;
  totalRows: number;
  selectedRows: number;
  created: number;
  createdWithWarnings: number;
  skippedDuplicates: number;
  failed: number;
}

export interface ImportBatchHistoryRow {
  rowNumber: number;
  outcome: ImportRowOutcomeStatus;
  companyId: number | null;
  jobOpportunityId: number | null;
  applicationId: number | null;
  duplicateJobOpportunityId: number | null;
  warnings: string[];
  errors: string[];
}

export interface ImportBatchDetail {
  batch: ImportBatchSummary;
  rows: ImportHistoryPage<ImportBatchHistoryRow>;
}
