import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";

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
import { getQuestions, type Question } from "../../questions/questionsApi";
import {
  getApplicationStatusHistory,
  getApplicationAutomation,
  automationAction,
  setApplicationAtsType,
  getApplicationPreparation,
  getApplicationPreparationEvents,
  preparationAction,
  type ApplicationAutomation,
  type ApplicationPreparation,
  type PreparationSessionEvent,
  type ApplicationStatusHistoryEvent,
  getApplicationLock,
  getApplicationLockHistory,
  applicationLockAction,
  type ApplicationLock,
  type ApplicationLockHistory,
} from "../api/applicationsApi";
import type { CareerMaterial } from "../../profile/types/careerMaterial";

interface TrackerRecordEditorProps {
  row: ApplicationTrackerRow;
  companies: Company[];
  jobs: JobOpportunity[];
  onSaveJob: (input: JobOpportunityInput) => Promise<JobOpportunity>;
  onSaveApplication: (input: ApplicationInput) => Promise<Application>;
  onAddApplication: (jobOpportunityId: number) => void;
  onMarkApplied: (row: ApplicationTrackerRow) => void;
  onClose: () => void;
  initialFocus?: TrackerEditorFocus;
  resumeMaterials?: CareerMaterial[];
}

export type TrackerEditorFocus =
  | "APPLICATION_DETAILS"
  | "FOLLOW_UP"
  | "INTERVIEW_PREPARATION";

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
    resumeMaterialId: row.resumeMaterialId,
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

function humanize(value: string): string {
  return value.toLowerCase().replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

export default function TrackerRecordEditor({
  row,
  companies,
  jobs,
  onSaveJob,
  onSaveApplication,
  onAddApplication,
  onMarkApplied,
  onClose,
  initialFocus,
  resumeMaterials = [],
}: TrackerRecordEditorProps) {
  const [jobMessage, setJobMessage] = useState("");
  const [applicationMessage, setApplicationMessage] = useState("");
  const [history, setHistory] = useState<ApplicationStatusHistoryEvent[]>([]);
  const [historyError, setHistoryError] = useState("");
  const [historyLoading, setHistoryLoading] = useState(row.applicationId !== null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [automation, setAutomation] = useState<ApplicationAutomation | null>(null);
  const [automationError, setAutomationError] = useState("");
  const [automationMessage,setAutomationMessage]=useState("");
  const [automationBusy,setAutomationBusy]=useState(false);
  const [preparation, setPreparation] = useState<ApplicationPreparation | null>(null);
  const [preparationEvents, setPreparationEvents] = useState<PreparationSessionEvent[]>([]);
  const [preparationError, setPreparationError] = useState("");
  const [preparationBusy, setPreparationBusy] = useState(false);
  const [applicationLock,setApplicationLock]=useState<ApplicationLock|null>(null);
  const [lockHistory,setLockHistory]=useState<ApplicationLockHistory[]>([]);
  const [lockError,setLockError]=useState("");const [lockMessage,setLockMessage]=useState("");const [lockBusy,setLockBusy]=useState(false);
  const applicationSectionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (initialFocus === "APPLICATION_DETAILS") {
      applicationSectionRef.current?.focus();
    }
  }, [initialFocus]);

  async function loadHistory(applicationId: number) {
    setHistoryError("");
    try {
      setHistory(await getApplicationStatusHistory(applicationId));
    } catch (caughtError) {
      setHistoryError(caughtError instanceof Error
        ? caughtError.message : "Status history could not be loaded.");
    } finally {
      setHistoryLoading(false);
    }
  }

  useEffect(() => {
    if (row.applicationId === null) return;
    let cancelled = false;
    getApplicationStatusHistory(row.applicationId)
      .then((events) => { if (!cancelled) setHistory(events); })
      .catch((caughtError: unknown) => {
        if (!cancelled) setHistoryError(caughtError instanceof Error
          ? caughtError.message : "Status history could not be loaded.");
      })
      .finally(() => { if (!cancelled) setHistoryLoading(false); });
    return () => { cancelled = true; };
  }, [row.applicationId]);

  async function loadLock(applicationId:number){const [lock,events]=await Promise.all([getApplicationLock(applicationId),getApplicationLockHistory(applicationId)]);setApplicationLock(lock);setLockHistory(events);}
  useEffect(()=>{if(row.applicationId===null)return;let cancelled=false;Promise.all([getApplicationLock(row.applicationId),getApplicationLockHistory(row.applicationId)]).then(([lock,events])=>{if(!cancelled){setApplicationLock(lock);setLockHistory(events);}}).catch((caught:unknown)=>{if(!cancelled)setLockError(caught instanceof Error?caught.message:"Application lock could not be loaded.");});return()=>{cancelled=true;};},[row.applicationId]);
  async function runLockAction(action:"mark-submitted"|"archive"|"restore"|"mark-testing"){if(row.applicationId===null)return;setLockBusy(true);setLockError("");setLockMessage("");try{const updated=await applicationLockAction(row.applicationId,action);setApplicationLock(updated);await loadLock(row.applicationId);setLockMessage(`Application lock updated: ${humanize(updated.lockState)}.`);}catch(caught){setLockError(caught instanceof Error?caught.message:"Application lock could not be updated.");}finally{setLockBusy(false);}}

  async function loadPreparation(applicationId: number) {
    const [state, events] = await Promise.all([
      getApplicationPreparation(applicationId),
      getApplicationPreparationEvents(applicationId),
    ]);
    setPreparation(state);
    setPreparationEvents(events);
  }

  async function runAutomationAction(action:"approve-prep"|"mark-ready"|"approve-submit"|"revoke"){
    if(row.applicationId===null)return;setAutomationBusy(true);setAutomationError("");setAutomationMessage("");
    try{const updated=await automationAction(row.applicationId,action);setAutomation(updated);setAutomationMessage(`Preparation permission updated: ${humanize(updated.state)}.`);}
    catch(caught){setAutomationError(caught instanceof Error?caught.message:"Preparation permission could not be updated.");}
    finally{setAutomationBusy(false);}
  }

  useEffect(() => {
    if (row.applicationId === null) return;
    let cancelled = false;
    Promise.all([
      getApplicationPreparation(row.applicationId),
      getApplicationPreparationEvents(row.applicationId),
    ]).then(([state, events]) => {
      if (!cancelled) { setPreparation(state); setPreparationEvents(events); }
    }).catch((caught: unknown) => {
      if (!cancelled) setPreparationError(caught instanceof Error
        ? caught.message : "Preparation session could not be loaded.");
    });
    return () => { cancelled = true; };
  }, [row.applicationId]);

  async function runPreparationAction(action: "initialize" | "cancel" | "retry" | "resume") {
    if (row.applicationId === null) return;
    setPreparationBusy(true);
    setPreparationError("");
    try {
      setPreparation(await preparationAction(row.applicationId, action));
      await loadPreparation(row.applicationId);
    } catch (caught) {
      setPreparationError(caught instanceof Error
        ? caught.message : "Preparation action could not be completed.");
    } finally {
      setPreparationBusy(false);
    }
  }

  useEffect(() => {
    if (row.applicationId === null) return;
    let cancelled = false;
    getApplicationAutomation(row.applicationId)
      .then((value) => { if (!cancelled) setAutomation(value); })
      .catch((caught: unknown) => { if (!cancelled) setAutomationError(
        caught instanceof Error ? caught.message : "Preparation permission could not be loaded."); });
    return () => { cancelled = true; };
  }, [row.applicationId]);

  useEffect(() => {
    if (row.applicationId === null) return;
    let cancelled = false;
    getQuestions(row.applicationId)
      .then((loaded) => { if (!cancelled) setQuestions(loaded); })
      .catch(() => { /* The history/editor remains usable if the summary fails. */ });
    return () => { cancelled = true; };
  }, [row.applicationId]);

  async function saveJob(input: JobOpportunityInput) {
    setJobMessage("");
    const result = await onSaveJob(input);
    setJobMessage("Job details saved.");
    return result;
  }

  async function saveApplication(input: ApplicationInput) {
    setApplicationMessage("");
    const result = await onSaveApplication(input);
    await loadHistory(result.id);
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
        <div
          ref={applicationSectionRef}
          tabIndex={-1}
          aria-label="Application Details editor"
        >
          <ApplicationForm
            key={`tracker-application-${row.applicationId}`}
            heading="Application Details"
            submitLabel="Save Application Details"
            jobs={jobs}
            initialValues={applicationInput(row)}
            resumeMaterials={resumeMaterials}
            resumeSelectionDisabled={applicationLock?.lockState === "SUBMITTED" || applicationLock?.lockState === "ARCHIVED"}
            isEditing
            onSubmit={saveApplication}
            onCancel={onClose}
            initialFocusField={
              initialFocus === "FOLLOW_UP"
                ? "followUpDate"
                : initialFocus === "INTERVIEW_PREPARATION"
                  ? "interviewTopics"
                  : undefined
            }
          />
          {applicationMessage && <p role="status">{applicationMessage}</p>}
          <section className="application-lock" aria-labelledby="application-lock-heading">
            <h3 id="application-lock-heading">Application Lock</h3>
            {lockError&&<p role="alert">{lockError}</p>}{lockMessage&&<p role="status">{lockMessage}</p>}
            {applicationLock?<><p><strong>{humanize(applicationLock.lockState)}</strong></p><p className="field-help">Lifecycle, preparation permission, and lock state are independent safety domains.</p><div className="approved-answer-actions">
              {applicationLock.lockState==="NOT_SUBMITTED"&&(row.status==="SAVED"||row.status==="PREPARING")&&<button type="button" disabled={lockBusy} onClick={()=>onMarkApplied(row)}>Mark as Applied</button>}
              {applicationLock.lockState==="NOT_SUBMITTED"&&<button type="button" disabled={lockBusy} onClick={()=>void runLockAction("mark-testing")}>Mark as Testing</button>}
              {applicationLock.lockState!=="ARCHIVED"&&<button type="button" disabled={lockBusy} onClick={()=>void runLockAction("archive")}>Archive Application</button>}
              {(applicationLock.lockState==="ARCHIVED"||applicationLock.lockState==="TESTING")&&<button type="button" disabled={lockBusy} onClick={()=>void runLockAction("restore")}>Restore</button>}
            </div><details><summary>Lock history ({lockHistory.length})</summary><ol>{lockHistory.map(event=><li key={event.id}>{humanize(event.newLock)} — {event.reason??humanize(event.source)}</li>)}</ol></details></>:<p>Loading application lock…</p>}
          </section>
          <section className="automation-summary" aria-labelledby="automation-summary-heading">
            <h3 id="automation-summary-heading">Application Preparation</h3>
            {automationError && <p role="alert">{automationError}</p>}
            {automationMessage&&<p role="status">{automationMessage}</p>}
            {automation && <>
              <p><strong>{humanize(automation.state)}</strong> · {humanize(automation.submissionMode)}</p>
              <p>{automation.unresolvedRequiredCount} unresolved required · {automation.blockerCount} blockers</p>
              {automation.blockReason && <p>{automation.blockReason}</p>}
              <label>ATS type <select value={automation.atsType} onChange={(event) =>
                void setApplicationAtsType(automation.applicationId,
                  event.target.value as ApplicationAutomation["atsType"]).then(setAutomation)}>
                {['UNKNOWN','GREENHOUSE','LEVER','ASHBY','WORKDAY','ICIMS','TALEO','CUSTOM'].map((value) =>
                  <option key={value} value={value}>{humanize(value)}</option>)}</select></label>
              <div className="approved-answer-actions">
                {automation.state === "NOT_APPROVED" && <button type="button" disabled={automationBusy || applicationLock?.lockState === "SUBMITTED" || applicationLock?.lockState === "ARCHIVED"} onClick={() =>
                  void runAutomationAction("approve-prep")}>{automationBusy?"Updating…":"Approve for Preparation"}</button>}
                {(automation.state === "APPROVED_FOR_PREP" || automation.state === "NEEDS_ANSWERS") &&
                  <button type="button" disabled={automationBusy || automation.unresolvedRequiredCount > 0 || automation.blockerCount > 0}
                    onClick={() => void runAutomationAction("mark-ready")}>Mark Ready for Review</button>}
                {automation.state === "READY_FOR_REVIEW" && <button type="button" disabled={automationBusy} onClick={() =>
                  void runAutomationAction("approve-submit")}>Approve to Submit</button>}
                {automation.state !== "NOT_APPROVED" && <button type="button" disabled={automationBusy} onClick={() =>
                  void runAutomationAction("revoke")}>Revoke Permission</button>}
              </div>
              {automation.state === "APPROVED_TO_SUBMIT" && <p>Approved to Submit means CareerOS has permission to submit this application once a supported submission mechanism exists. No submission occurs from this action today.</p>}
            </>}
          </section>
          <section className="question-summary" aria-labelledby="question-summary-heading">
            <h3 id="question-summary-heading">Application Questions</h3>
            <p>{questions.filter((question) => question.status === "UNANSWERED").length} unanswered · {questions.filter((question) => question.status === "NEEDS_REVIEW").length} needs review · {questions.filter((question) => question.status === "BLOCKED").length} blockers</p>
            <Link to={`/questions?applicationId=${row.applicationId}`}>Open Question Queue</Link>
            <Link to={`/questions?applicationId=${row.applicationId}#question-mapping-review`}>Review Question Mappings</Link>
          </section>
          <section className="preparation-session" aria-labelledby="preparation-session-heading">
            <h3 id="preparation-session-heading">Preparation Session</h3>
            {preparationError && <p role="alert">{preparationError}</p>}
            {preparation ? <>
              <p><strong>Capability:</strong> {humanize(preparation.capability)}</p>
              <p><strong>Session:</strong> {preparation.session
                ? humanize(preparation.session.state) : "Not initialized"}</p>
              {preparation.session && <p className="field-help">
                {preparation.capability === "FIELD_PREPARATION"
                  ? "This build can prepare fields from verified profile data and reusable approved answers. It cannot submit applications."
                  : preparation.capability === "INSPECTION"
                    ? "This build can inspect supported application forms and record questions. It cannot fill or submit applications."
                    : "This build records preparation sessions only. It does not open or inspect the application form."}
              </p>}
              {preparation.session?.state === "WAITING_FOR_USER" && <p>
                Paused{preparation.session.currentPage ? ` at ${preparation.session.currentPage}` : ""}.
                {preparation.session.currentQuestion ? ` Current question: ${preparation.session.currentQuestion}.` : ""}
              </p>}
              <div className="approved-answer-actions">
                {!preparation.session && <button type="button" disabled={preparationBusy || applicationLock?.lockState === "SUBMITTED" || applicationLock?.lockState === "ARCHIVED"}
                  onClick={() => void runPreparationAction("initialize")}>Initialize Preparation</button>}
                {preparation.session && ["INITIALIZED", "OPENING", "COLLECTING_QUESTIONS", "WAITING_FOR_USER", "PREPARING_FIELDS"].includes(preparation.session.state) &&
                  <button type="button" disabled={preparationBusy}
                    onClick={() => void runPreparationAction("cancel")}>Cancel Preparation</button>}
                {preparation.session?.state === "WAITING_FOR_USER" &&
                  <button type="button" disabled={preparationBusy}
                    onClick={() => void runPreparationAction("resume")}>Resume Preparation</button>}
                {preparation.session && ["FAILED", "CANCELLED"].includes(preparation.session.state) &&
                  <button type="button" disabled={preparationBusy}
                    onClick={() => void runPreparationAction("retry")}>Retry Preparation</button>}
              </div>
              <h4>Session Events</h4>
              {preparationEvents.length === 0 ? <p>No preparation events recorded.</p> :
                <ol>{preparationEvents.map((event) => <li key={event.id}>
                  <strong>{humanize(event.eventType)}</strong>{" — "}{event.safeUserMessage}
                  <span>{new Intl.DateTimeFormat("en-US", {
                    dateStyle: "medium", timeStyle: "short",
                  }).format(new Date(event.timestamp))}</span>
                </li>)}</ol>}
            </> : <p>Loading preparation capability...</p>}
          </section>
          <section className="status-history" aria-labelledby="status-history-heading">
            <h3 id="status-history-heading">Status History</h3>
            {historyLoading ? <p>Loading status history...</p>
              : historyError ? <p role="alert">{historyError}</p>
                : history.length === 0 ? <p>No status history recorded yet.</p>
                  : <ol>{history.map((event) => (
                    <li key={event.id}>
                      <strong>{event.previousStatus === null
                        ? humanize(event.newStatus)
                        : `${humanize(event.previousStatus)} → ${humanize(event.newStatus)}`}</strong>
                      <span>{new Intl.DateTimeFormat("en-US", {
                        dateStyle: "medium", timeStyle: "short",
                      }).format(new Date(event.occurredAt))}</span>
                      <span>{humanize(event.source)}</span>
                      {event.note && <p>{event.note}</p>}
                    </li>
                  ))}</ol>}
          </section>
        </div>
      )}
    </section>
  );
}
