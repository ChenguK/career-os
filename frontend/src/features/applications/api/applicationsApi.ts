import { apiRequest } from "../../../shared/api/apiClient";
import type {
  Application,
  ApplicationInput,
} from "../types/application";

export function getApplications(): Promise<Application[]> {
  return apiRequest<Application[]>("/api/applications");
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