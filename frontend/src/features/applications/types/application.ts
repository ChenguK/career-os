export type ApplicationStatus =
  | "SAVED"
  | "PREPARING"
  | "APPLIED"
  | "PHONE_SCREEN"
  | "INTERVIEW_ONE"
  | "INTERVIEW_TWO"
  | "OFFER"
  | "REJECTED"
  | "WITHDRAWN"
  | "CLOSED";

export interface Application {
  id: number;
  jobOpportunityId: number;
  positionTitle: string;
  companyId: number | null;
  companyName: string | null;
  status: ApplicationStatus;
  resumeVersion: string | null;
  resumeMaterialId?: number | null;
  resumeMaterialDisplayName?: string | null;
  resumeMaterialActive?: boolean;
  coverLetterNeeded: boolean;
  portfolioLink: string | null;
  githubLink: string | null;
  projectsToHighlight: string | null;
  skillsToEmphasize: string | null;
  interviewTopics: string | null;
  recruiterName: string | null;
  recruiterEmail: string | null;
  applicationDate: string | null;
  followUpDate: string | null;
  phoneScreenAt: string | null;
  interviewOneAt: string | null;
  interviewTwoAt: string | null;
  offerAt: string | null;
  rejectedAt: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationInput {
  jobOpportunityId: number | null;
  status: ApplicationStatus;
  resumeVersion: string;
  resumeMaterialId?: number | null;
  coverLetterNeeded: boolean;
  portfolioLink: string;
  githubLink: string;
  projectsToHighlight: string;
  skillsToEmphasize: string;
  interviewTopics: string;
  recruiterName: string;
  recruiterEmail: string;
  applicationDate: string;
  followUpDate: string;
  phoneScreenAt: string;
  interviewOneAt: string;
  interviewTwoAt: string;
  offerAt: string;
  rejectedAt: string;
  notes: string;
}
