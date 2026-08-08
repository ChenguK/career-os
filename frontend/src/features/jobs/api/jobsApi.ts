import { apiRequest } from "../../../shared/api/apiClient";
import type {
  JobOpportunity,
  JobOpportunityInput,
} from "../types/job";

export function getJobs(
  search = "",
): Promise<JobOpportunity[]> {
  const query = search.trim()
    ? `?search=${encodeURIComponent(search.trim())}`
    : "";

  return apiRequest<JobOpportunity[]>(`/api/jobs${query}`);
}

export function createJob(
  input: JobOpportunityInput,
): Promise<JobOpportunity> {
  return apiRequest<JobOpportunity>("/api/jobs", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });
}

export function updateJob(
  id: number,
  input: JobOpportunityInput,
): Promise<JobOpportunity> {
  return apiRequest<JobOpportunity>(`/api/jobs/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });
}

export function deleteJob(id: number): Promise<void> {
  return apiRequest<void>(`/api/jobs/${id}`, {
    method: "DELETE",
  });
}