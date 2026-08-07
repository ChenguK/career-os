export type RemoteType =
  | "REMOTE"
  | "HYBRID"
  | "ONSITE"
  | "UNKNOWN";

export interface JobOpportunity {
  id: number;
  companyId: number | null;
  companyName: string | null;
  positionTitle: string;
  department: string | null;
  location: string | null;
  remoteType: RemoteType;
  employmentType: string | null;
  salaryMin: number | null;
  salaryMax: number | null;
  salaryCurrency: string;
  salaryNotes: string | null;
  applicationUrl: string | null;
  source: string | null;
  datePosted: string | null;
  closingDate: string | null;
  priority: number;
  matchScore: number | null;
  jobDescription: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface JobOpportunityInput {
  companyId: number | null;
  positionTitle: string;
  department: string;
  location: string;
  remoteType: RemoteType;
  employmentType: string;
  salaryMin: number | null;
  salaryMax: number | null;
  salaryCurrency: string;
  salaryNotes: string;
  applicationUrl: string;
  source: string;
  datePosted: string;
  closingDate: string;
  priority: number;
  matchScore: number | null;
  jobDescription: string;
  notes: string;
}