import {
  apiDownload,
  apiRequest,
  type ApiDownload,
} from "../../../shared/api/apiClient";
import type {
  Application,
  ApplicationInput,
} from "../types/application";
import type {
  ApplicationTrackerPage,
  ApplicationTrackerQuery,
  ApplicationTrackerRow,
} from "../types/applicationTracker";
import type {
  ImportPersistenceRequest,
  ImportPersistenceResponse,
  ImportBatchDetail,
  ImportBatchSummary,
  ImportHistoryPage,
  ImportPreviewResponse,
} from "../types/importPreview";

export function getApplications(): Promise<Application[]> {
  return apiRequest<Application[]>("/api/applications");
}

export interface ApplicationStatusHistoryEvent {
  id: number;
  applicationId: number;
  previousStatus: string | null;
  newStatus: string;
  occurredAt: string;
  source: "USER" | "IMPORT" | "SYSTEM" | "AUTOMATION";
  note: string | null;
  createdAt: string;
}

export interface ApplicationAutomation {
  id: number; applicationId: number;
  state: "NOT_APPROVED" | "APPROVED_FOR_PREP" | "NEEDS_ANSWERS" | "READY_FOR_REVIEW" | "APPROVED_TO_SUBMIT" | "BLOCKED";
  submissionMode: "PREPARE_ONLY" | "REQUIRE_REVIEW_BEFORE_SUBMIT";
  atsType: "GREENHOUSE" | "LEVER" | "ASHBY" | "WORKDAY" | "ICIMS" | "TALEO" | "CUSTOM" | "UNKNOWN";
  unresolvedRequiredCount: number; needsReviewCount: number; blockerCount: number;
  blockReason: string | null; approvedForPrepAt: string | null;
  readyForReviewAt: string | null; approvedToSubmitAt: string | null; updatedAt: string;
}

export function getApplicationAutomation(id: number): Promise<ApplicationAutomation> {
  return apiRequest<ApplicationAutomation>(`/api/applications/${id}/automation`);
}
export function automationAction(id: number, action: "approve-prep" | "mark-ready" | "approve-submit" | "revoke"): Promise<ApplicationAutomation> {
  return apiRequest<ApplicationAutomation>(`/api/applications/${id}/automation/${action}`, { method: "POST" });
}
export function setApplicationAtsType(id: number, atsType: ApplicationAutomation["atsType"]): Promise<ApplicationAutomation> {
  return apiRequest<ApplicationAutomation>(`/api/applications/${id}/automation/ats-type`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ atsType }) });
}

export type PreparationCapability = "NONE" | "SESSION_ONLY" | "INSPECTION" |
  "FIELD_PREPARATION" | "READY_FOR_REVIEW";
export type PreparationSessionState = "INITIALIZED" | "OPENING" |
  "COLLECTING_QUESTIONS" | "WAITING_FOR_USER" | "PREPARING_FIELDS" |
  "READY_FOR_REVIEW" | "FAILED" | "CANCELLED";
export interface PreparationSession {
  id: number; applicationId: number; formTargetId: number;
  previousSessionId: number | null; state: PreparationSessionState;
  normalizedFormUrl: string; startedAt: string; lastProgressAt: string;
  completedAt: string | null; createdAt: string; updatedAt: string;
  currentPage?: string | null; currentQuestion?: string | null;
  checkpoint?: string | null; snapshotHash?: string | null;
  resumeState?: PreparationSessionState | null; pausedAt?: string | null;
}
export interface ApplicationPreparation {
  capability: PreparationCapability;
  session: PreparationSession | null;
}
export interface PreparationSessionEvent {
  id: number; sessionId: number; eventType: string; timestamp: string;
  retryable: boolean; safeUserMessage: string;
  pageKey: string | null; questionKey: string | null;
}

export function getApplicationPreparation(id: number): Promise<ApplicationPreparation> {
  return apiRequest<ApplicationPreparation>(`/api/applications/${id}/preparation`);
}
export function getApplicationPreparationEvents(id: number): Promise<PreparationSessionEvent[]> {
  return apiRequest<PreparationSessionEvent[]>(`/api/applications/${id}/preparation/events`);
}
export function preparationAction(id: number, action: "initialize" | "cancel" | "retry" | "resume"): Promise<ApplicationPreparation> {
  return apiRequest<ApplicationPreparation>(`/api/applications/${id}/preparation/${action}`, { method: "POST" });
}

export function getApplicationStatusHistory(
  applicationId: number,
): Promise<ApplicationStatusHistoryEvent[]> {
  return apiRequest<ApplicationStatusHistoryEvent[]>(
    `/api/applications/${applicationId}/history`,
  );
}

export function getApplicationTracker(
  query: ApplicationTrackerQuery = {},
): Promise<ApplicationTrackerPage> {
  const parameters = trackerParameters(query, true);
  const encoded = parameters.toString();
  const url = `/api/applications/tracker${
    encoded ? `?${encoded}` : ""
  }`;

  return apiRequest<ApplicationTrackerPage>(url);
}

export function getApplicationTrackerRow(
  jobOpportunityId: number,
): Promise<ApplicationTrackerRow> {
  return apiRequest<ApplicationTrackerRow>(
    `/api/applications/tracker/jobs/${jobOpportunityId}`,
  );
}

function trackerParameters(
  query: ApplicationTrackerQuery,
  includePagination: boolean,
): URLSearchParams {
  const parameters = new URLSearchParams();

  function appendValues(
    name: string,
    values: Array<string | number> | undefined,
  ) {
    values?.forEach((value) => {
      parameters.append(name, String(value));
    });
  }

  if (query.search?.trim()) {
    parameters.set("search", query.search.trim());
  }
  appendValues("statuses", query.statuses);
  appendValues("priorities", query.priorities);
  appendValues("remoteTypes", query.remoteTypes);

  const scalarParameters = {
    companyId: query.companyId,
    applicationDateFrom: query.applicationDateFrom,
    applicationDateTo: query.applicationDateTo,
    datePostedFrom: query.datePostedFrom,
    datePostedTo: query.datePostedTo,
    followUpDateFrom: query.followUpDateFrom,
    followUpDateTo: query.followUpDateTo,
    sort: query.sort,
    direction: query.direction,
    ...(includePagination
      ? { page: query.page, size: query.size }
      : {}),
  };

  Object.entries(scalarParameters).forEach(([name, value]) => {
    if (value !== undefined && value !== "") {
      parameters.set(name, String(value));
    }
  });

  return parameters;
}

export function exportApplicationTrackerCsv(
  mode: "CURRENT_VIEW" | "ALL",
  query: ApplicationTrackerQuery = {},
): Promise<ApiDownload> {
  const parameters = mode === "CURRENT_VIEW"
    ? trackerParameters(query, false)
    : new URLSearchParams();
  parameters.set("mode", mode);

  return apiDownload(
    `/api/applications/tracker/export.csv?${parameters.toString()}`,
  );
}

export function exportApplicationTrackerXlsx(
  mode: "CURRENT_VIEW" | "ALL",
  query: ApplicationTrackerQuery = {},
): Promise<ApiDownload> {
  const parameters = mode === "CURRENT_VIEW"
    ? trackerParameters(query, false)
    : new URLSearchParams();
  parameters.set("mode", mode);

  return apiDownload(
    `/api/applications/tracker/export.xlsx?${parameters.toString()}`,
  );
}

export function createApplication(
  input: ApplicationInput,
): Promise<Application> {
  return apiRequest<Application>("/api/applications", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });
}

export function updateApplication(
  id: number,
  input: ApplicationInput,
): Promise<Application> {
  return apiRequest<Application>(`/api/applications/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });
}

export function deleteApplication(id: number): Promise<void> {
  return apiRequest<void>(`/api/applications/${id}`, {
    method: "DELETE",
  });
}

export function previewCsvImport(
  file: File,
): Promise<ImportPreviewResponse> {
  const formData = new FormData();
  formData.append("file", file);

  return apiRequest<ImportPreviewResponse>(
    "/api/applications/import/preview",
    {
      method: "POST",
      body: formData,
    },
  );
}

export function persistCsvImport(
  request: ImportPersistenceRequest,
): Promise<ImportPersistenceResponse> {
  return apiRequest<ImportPersistenceResponse>(
    "/api/applications/import",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    },
  );
}

export function getImportHistory(
  page = 0,
  size = 25,
): Promise<ImportHistoryPage<ImportBatchSummary>> {
  return apiRequest<ImportHistoryPage<ImportBatchSummary>>(
    `/api/applications/imports?page=${page}&size=${size}`,
  );
}

export function getImportBatch(
  batchId: number,
  page = 0,
  size = 25,
): Promise<ImportBatchDetail> {
  return apiRequest<ImportBatchDetail>(
    `/api/applications/imports/${batchId}?page=${page}&size=${size}`,
  );
}
