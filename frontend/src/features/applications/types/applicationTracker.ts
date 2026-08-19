import type { RemoteType } from "../../jobs/types/job";
import type { ApplicationStatus } from "./application";

export interface ApplicationTrackerRow {
  jobOpportunityId: number;
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
  jobNotes: string | null;
  jobCreatedAt: string;
  jobUpdatedAt: string;
  applicationId: number | null;
  status: ApplicationStatus | null;
  resumeVersion: string | null;
  resumeMaterialId?: number | null;
  resumeMaterialDisplayName?: string | null;
  resumeMaterialActive?: boolean | null;
  coverLetterNeeded: boolean | null;
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
  applicationNotes: string | null;
  applicationCreatedAt: string | null;
  applicationUpdatedAt: string | null;
  automationState?: "NOT_APPROVED" | "APPROVED_FOR_PREP" | "NEEDS_ANSWERS" |
    "READY_FOR_REVIEW" | "APPROVED_TO_SUBMIT" | "BLOCKED" | null;
  statusDate?: string | null;
  lockState?: "NOT_SUBMITTED" | "SUBMITTED" | "ARCHIVED" | "TESTING" | null;
}

export type ApplicationTrackerSort =
  | "company"
  | "positionTitle"
  | "status"
  | "priority"
  | "matchScore"
  | "location"
  | "remoteType"
  | "salaryMin"
  | "datePosted"
  | "applicationDate"
  | "followUpDate"
  | "source"
  | "createdAt";

export interface ApplicationTrackerQuery {
  search?: string;
  statuses?: ApplicationStatus[];
  priorities?: number[];
  remoteTypes?: RemoteType[];
  companyId?: number;
  applicationDateFrom?: string;
  applicationDateTo?: string;
  datePostedFrom?: string;
  datePostedTo?: string;
  followUpDateFrom?: string;
  followUpDateTo?: string;
  sort?: ApplicationTrackerSort;
  direction?: "asc" | "desc";
  page?: number;
  size?: number;
}

export interface ApplicationTrackerPage {
  content: ApplicationTrackerRow[];
  page: number;
  size: number;
  totalRows: number;
  totalPages: number;
}
