import type { RemoteType } from "../../jobs/types/job";

export interface ApplicantProfile {
  exists: boolean;
  id: number | null;
  firstName: string | null;
  lastName: string | null;
  preferredName: string | null;
  email: string | null;
  phone: string | null;
  city: string | null;
  stateRegion: string | null;
  country: string | null;
  postalCode: string | null;
  portfolioUrl: string | null;
  githubUrl: string | null;
  linkedinUrl: string | null;
  defaultResumeVersion: string | null;
  defaultResumeMaterialId?: number | null;
  preferredWorkArrangement: RemoteType;
  minimumSalary: number | null;
  salaryCurrency: string;
  willingToRelocate: boolean | null;
  willingToTravel: boolean | null;
  verified: boolean;
  lastVerifiedAt: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface ApplicantProfileInput {
  firstName: string;
  lastName: string;
  preferredName: string;
  email: string;
  phone: string;
  city: string;
  stateRegion: string;
  country: string;
  postalCode: string;
  portfolioUrl: string;
  githubUrl: string;
  linkedinUrl: string;
  defaultResumeVersion: string;
  preferredWorkArrangement: RemoteType;
  minimumSalary: number | null;
  salaryCurrency: string;
  willingToRelocate: boolean | null;
  willingToTravel: boolean | null;
}

export const emptyApplicantProfileInput: ApplicantProfileInput = {
  firstName: "",
  lastName: "",
  preferredName: "",
  email: "",
  phone: "",
  city: "",
  stateRegion: "",
  country: "",
  postalCode: "",
  portfolioUrl: "",
  githubUrl: "",
  linkedinUrl: "",
  defaultResumeVersion: "",
  preferredWorkArrangement: "UNKNOWN",
  minimumSalary: null,
  salaryCurrency: "USD",
  willingToRelocate: null,
  willingToTravel: null,
};

export function profileToInput(
  profile: ApplicantProfile,
): ApplicantProfileInput {
  return {
    firstName: profile.firstName ?? "",
    lastName: profile.lastName ?? "",
    preferredName: profile.preferredName ?? "",
    email: profile.email ?? "",
    phone: profile.phone ?? "",
    city: profile.city ?? "",
    stateRegion: profile.stateRegion ?? "",
    country: profile.country ?? "",
    postalCode: profile.postalCode ?? "",
    portfolioUrl: profile.portfolioUrl ?? "",
    githubUrl: profile.githubUrl ?? "",
    linkedinUrl: profile.linkedinUrl ?? "",
    defaultResumeVersion: profile.defaultResumeVersion ?? "",
    preferredWorkArrangement: profile.preferredWorkArrangement,
    minimumSalary: profile.minimumSalary,
    salaryCurrency: profile.salaryCurrency,
    willingToRelocate: profile.willingToRelocate,
    willingToTravel: profile.willingToTravel,
  };
}
