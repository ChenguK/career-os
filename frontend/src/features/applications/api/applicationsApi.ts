import { apiRequest } from "../../../shared/api/apiClient";
import type {
  Application,
  ApplicationInput,
} from "../types/application";
import type {
  ApplicationTrackerPage,
  ApplicationTrackerQuery,
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

export function getApplicationTracker(
  query: ApplicationTrackerQuery = {},
): Promise<ApplicationTrackerPage> {
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
    page: query.page,
    size: query.size,
  };

  Object.entries(scalarParameters).forEach(([name, value]) => {
    if (value !== undefined && value !== "") {
      parameters.set(name, String(value));
    }
  });

  const encoded = parameters.toString();
  const url = `/api/applications/tracker${
    encoded ? `?${encoded}` : ""
  }`;

  return apiRequest<ApplicationTrackerPage>(url);
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
