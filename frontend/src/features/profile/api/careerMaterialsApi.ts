import { apiRequest } from "../../../shared/api/apiClient";
import type { CareerMaterial } from "../types/careerMaterial";

export const getCareerMaterials = () =>
  apiRequest<CareerMaterial[]>("/api/applicant-profile/materials");

export function uploadCareerMaterial(file: File, fields: {
  displayName: string; targetJobFamily: string; targetSeniority: string;
  versionLabel: string; notes: string;
}) {
  const body = new FormData(); body.append("file", file);
  Object.entries(fields).forEach(([key,value]) => { if(value.trim()) body.append(key,value.trim()); });
  return apiRequest<CareerMaterial>("/api/applicant-profile/materials", { method:"POST", body });
}
export const setDefaultCareerMaterial = (id:number) => apiRequest<CareerMaterial>(`/api/applicant-profile/materials/${id}/default`,{method:"POST"});
export const deactivateCareerMaterial = (id:number) => apiRequest<CareerMaterial>(`/api/applicant-profile/materials/${id}/deactivate`,{method:"POST"});
export const deleteCareerMaterial = (id:number) => apiRequest<void>(`/api/applicant-profile/materials/${id}`,{method:"DELETE"});
export const careerMaterialDownloadUrl = (id:number) => `/api/applicant-profile/materials/${id}/download`;
