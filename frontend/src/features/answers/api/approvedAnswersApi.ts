import { apiRequest } from "../../../shared/api/apiClient";
import type {
  ApprovedAnswer,
  ApprovedAnswerInput,
} from "../types/approvedAnswer";

const basePath = "/api/approved-answers";

export function getApprovedAnswers(): Promise<ApprovedAnswer[]> {
  return apiRequest<ApprovedAnswer[]>(basePath);
}

export function createApprovedAnswer(
  input: ApprovedAnswerInput,
): Promise<ApprovedAnswer> {
  return apiRequest<ApprovedAnswer>(basePath, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
}

export function updateApprovedAnswer(
  id: number,
  input: ApprovedAnswerInput,
): Promise<ApprovedAnswer> {
  return apiRequest<ApprovedAnswer>(`${basePath}/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
}

export function approveAnswer(id: number): Promise<ApprovedAnswer> {
  return apiRequest<ApprovedAnswer>(`${basePath}/${id}/approve`, {
    method: "POST",
  });
}

export function revokeAnswer(id: number): Promise<ApprovedAnswer> {
  return apiRequest<ApprovedAnswer>(`${basePath}/${id}/revoke`, {
    method: "POST",
  });
}

export function deleteApprovedAnswer(id: number): Promise<void> {
  return apiRequest<void>(`${basePath}/${id}`, { method: "DELETE" });
}
