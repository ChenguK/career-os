import type { JobOpportunityInput } from "./job";

export const emptyJobInput: JobOpportunityInput = {
  companyId: null,
  positionTitle: "",
  department: "",
  location: "",
  remoteType: "UNKNOWN",
  employmentType: "",
  salaryMin: null,
  salaryMax: null,
  salaryCurrency: "USD",
  salaryNotes: "",
  applicationUrl: "",
  source: "",
  datePosted: "",
  closingDate: "",
  priority: 3,
  matchScore: null,
  jobDescription: "",
  notes: "",
};