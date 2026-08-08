import { apiRequest } from "../../../shared/api/apiClient";
import type {
  Company,
  CompanyInput,
} from "../types/company";

export function getCompanies(
  search = "",
): Promise<Company[]> {
  const query = search.trim()
    ? `?search=${encodeURIComponent(search.trim())}`
    : "";

  return apiRequest<Company[]>(`/api/companies${query}`);
}

export function createCompany(
  input: CompanyInput,
): Promise<Company> {
  return apiRequest<Company>("/api/companies", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });
}

export function updateCompany(
  id: number,
  input: CompanyInput,
): Promise<Company> {
  return apiRequest<Company>(`/api/companies/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });
}

export function deleteCompany(id: number): Promise<void> {
  return apiRequest<void>(`/api/companies/${id}`, {
    method: "DELETE",
  });
}