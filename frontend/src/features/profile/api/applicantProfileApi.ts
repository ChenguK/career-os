import { apiRequest } from "../../../shared/api/apiClient";
import type {
  ApplicantProfile,
  ApplicantProfileInput,
} from "../types/applicantProfile";

export function getApplicantProfile(): Promise<ApplicantProfile> {
  return apiRequest<ApplicantProfile>("/api/applicant-profile");
}

export function saveApplicantProfile(
  input: ApplicantProfileInput,
): Promise<ApplicantProfile> {
  return apiRequest<ApplicantProfile>("/api/applicant-profile", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
}

export function verifyApplicantProfile(): Promise<ApplicantProfile> {
  return apiRequest<ApplicantProfile>("/api/applicant-profile/verify", {
    method: "POST",
  });
}
