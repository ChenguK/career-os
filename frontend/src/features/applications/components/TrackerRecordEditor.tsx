import { useState } from "react";

import type { Company } from "../../companies/types/company";
import JobForm from "../../jobs/components/JobForm";
import type {
  JobOpportunity,
  JobOpportunityInput,
} from "../../jobs/types/job";
import ApplicationForm from "./ApplicationForm";
import type {
  Application,
  ApplicationInput,
} from "../types/application";
import type { ApplicationTrackerRow } from "../types/applicationTracker";

interface TrackerRecordEditorProps {
  row: ApplicationTrackerRow;
  companies: Company[];
  jobs: JobOpportunity[];
  onSaveJob: (input: JobOpportunityInput) => Promise<JobOpportunity>;
  onSaveApplication: (input: ApplicationInput) => Promise<Application>;
  onAddApplication: (jobOpportunityId: number) => void;
  onClose: () => void;
}

function toLocalDateTimeInput(value: string | null): string {
  if (!value) {
    return "";
  }

  const date = new Date(value);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function jobInput(row: ApplicationTrackerRow): JobOpportunityInput {
  return {
    companyId: row.companyId,
    positionTitle: row.positionTitle,
    department: row.department ?? "",
    location: row.location ?? "",
    remoteType: row.remoteType,
    employmentType: row.employmentType ?? "",
    salaryMin: row.salaryMin,
    salaryMax: row.salaryMax,
    salaryCurrency: row.salaryCurrency,
    salaryNotes: row.salaryNotes ?? "",
    applicationUrl: row.applicationUrl ?? "",
    source: row.source ?? "",
    datePosted: row.datePosted ?? "",
    closingDate: row.closingDate ?? "",
    priority: row.priority,
    matchScore: row.matchScore,
    jobDescription: row.jobDescription ?? "",
    notes: row.jobNotes ?? "",
  };
}

function applicationInput(row: ApplicationTrackerRow): ApplicationInput {
  return {
    jobOpportunityId: row.jobOpportunityId,
    status: row.status ?? "SAVED",
    resumeVersion: row.resumeVersion ?? "",
    coverLetterNeeded: row.coverLetterNeeded ?? false,
    portfolioLink: row.portfolioLink ?? "",
    githubLink: row.githubLink ?? "",
    projectsToHighlight: row.projectsToHighlight ?? "",
    skillsToEmphasize: row.skillsToEmphasize ?? "",
    interviewTopics: row.interviewTopics ?? "",
    recruiterName: row.recruiterName ?? "",
    recruiterEmail: row.recruiterEmail ?? "",
    applicationDate: row.applicationDate ?? "",
    followUpDate: row.followUpDate ?? "",
    phoneScreenAt: toLocalDateTimeInput(row.phoneScreenAt),
    interviewOneAt: toLocalDateTimeInput(row.interviewOneAt),
    interviewTwoAt: toLocalDateTimeInput(row.interviewTwoAt),
    offerAt: toLocalDateTimeInput(row.offerAt),
    rejectedAt: toLocalDateTimeInput(row.rejectedAt),
    notes: row.applicationNotes ?? "",
  };
}

export default function TrackerRecordEditor({
  row,
  companies,
  jobs,
  onSaveJob,
  onSaveApplication,
  onAddApplication,
  onClose,
}: TrackerRecordEditorProps) {
  const [jobMessage, setJobMessage] = useState("");
  const [applicationMessage, setApplicationMessage] = useState("");

  async function saveJob(input: JobOpportunityInput) {
    setJobMessage("");
    const result = await onSaveJob(input);
    setJobMessage("Job details saved.");
    return result;
  }

  async function saveApplication(input: ApplicationInput) {
    setApplicationMessage("");
    const result = await onSaveApplication(input);
    setApplicationMessage("Application details saved.");
    return result;
  }

  return (
    <section
      className="tracker-record-editor"
      aria-labelledby="tracker-record-editor-heading"
    >
      <div className="tracker-record-editor__header">
        <h2 id="tracker-record-editor-heading">
          Edit {row.positionTitle}
        </h2>
        <button type="button" onClick={onClose}>
          Close editor
        </button>
      </div>

      <JobForm
        key={`tracker-job-${row.jobOpportunityId}`}
        heading="Job Details"
        submitLabel="Save Job Details"
        companies={companies}
        initialValues={jobInput(row)}
        onSubmit={saveJob}
        onCancel={onClose}
      />
      {jobMessage && <p role="status">{jobMessage}</p>}

      {row.applicationId === null ? (
        <section aria-labelledby="tracker-application-details-heading">
          <h2 id="tracker-application-details-heading">Application Details</h2>
          <p>No Application exists for this job yet.</p>
          <button
            type="button"
            onClick={() => onAddApplication(row.jobOpportunityId)}
          >
            Add Application
          </button>
        </section>
      ) : (
        <>
          <ApplicationForm
            key={`tracker-application-${row.applicationId}`}
            heading="Application Details"
            submitLabel="Save Application Details"
            jobs={jobs}
            initialValues={applicationInput(row)}
            isEditing
            onSubmit={saveApplication}
            onCancel={onClose}
          />
          {applicationMessage && <p role="status">{applicationMessage}</p>}
        </>
      )}
    </section>
  );
}
