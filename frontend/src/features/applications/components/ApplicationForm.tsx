import {
  type ChangeEvent,
  type SyntheticEvent,
  useState,
} from "react";

import type { JobOpportunity } from "../../jobs/types/job";
import type {
  Application,
  ApplicationInput,
  ApplicationStatus,
} from "../types/application";
import { emptyApplicationInput } from "../types/applicationFormDefaults";
import type { CareerMaterial } from "../../profile/types/careerMaterial";

interface ApplicationFormProps {
  heading: string;
  submitLabel: string;
  jobs: JobOpportunity[];
  initialValues?: ApplicationInput;
  isEditing?: boolean;
  onSubmit: (
    input: ApplicationInput,
  ) => Promise<Application>;
  onCancel?: () => void;
  initialFocusField?:
    | "jobOpportunityId"
    | "followUpDate"
    | "interviewTopics";
  resumeMaterials?: CareerMaterial[];
  resumeSelectionDisabled?: boolean;
}

function toApiDateTime(value: string): string {
  if (!value) {
    return "";
  }

  return new Date(value).toISOString();
}

export default function ApplicationForm({
  heading,
  submitLabel,
  jobs,
  initialValues = emptyApplicationInput,
  isEditing = false,
  onSubmit,
  onCancel,
  initialFocusField,
  resumeMaterials = [],
  resumeSelectionDisabled = false,
}: ApplicationFormProps) {
  const [form, setForm] =
    useState<ApplicationInput>(initialValues);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  function handleTextChange(
    event: ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >,
  ) {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  }

  function handleJobChange(
    event: ChangeEvent<HTMLSelectElement>,
  ) {
    const value = event.target.value;

    setForm((current) => ({
      ...current,
      jobOpportunityId: value ? Number(value) : null,
    }));
  }

  function handleCheckboxChange(
    event: ChangeEvent<HTMLInputElement>,
  ) {
    const { name, checked } = event.target;

    setForm((current) => ({
      ...current,
      [name]: checked,
    }));
  }

  async function handleSubmit(
    event: SyntheticEvent<HTMLFormElement, SubmitEvent>,
  ) {
    event.preventDefault();

    setError("");
    setIsSubmitting(true);

    try {
      const normalizedInput: ApplicationInput = {
        ...form,
        phoneScreenAt: toApiDateTime(form.phoneScreenAt),
        interviewOneAt: toApiDateTime(form.interviewOneAt),
        interviewTwoAt: toApiDateTime(form.interviewTwoAt),
        offerAt: toApiDateTime(form.offerAt),
        rejectedAt: toApiDateTime(form.rejectedAt),
      };

      await onSubmit(normalizedInput);

      if (!isEditing) {
        setForm(emptyApplicationInput);
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "The application could not be saved.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  const statuses: {
    value: ApplicationStatus;
    label: string;
    disabled?: boolean;
  }[] = [
    { value: "SAVED", label: "Saved" },
    { value: "PREPARING", label: "Preparing" },
    { value: "APPLIED", label: form.status === "APPLIED" ? "Applied" : "Applied (use Mark as Applied)", disabled: form.status !== "APPLIED" },
    { value: "PHONE_SCREEN", label: "Phone Screen" },
    { value: "INTERVIEW_ONE", label: "Interview 1" },
    { value: "INTERVIEW_TWO", label: "Interview 2" },
    { value: "OFFER", label: "Offer" },
    { value: "REJECTED", label: "Rejected" },
    { value: "WITHDRAWN", label: "Withdrawn" },
    { value: "CLOSED", label: "Closed" },
  ];

  return (
    <section aria-labelledby={`${heading}-heading`}>
      <h2 id={`${heading}-heading`}>{heading}</h2>

      <form
        className="application-form"
        onSubmit={handleSubmit}
      >
        <label>
          Job opportunity
          <select
            name="jobOpportunityId"
            value={form.jobOpportunityId ?? ""}
            onChange={handleJobChange}
            disabled={isEditing}
            required
            autoFocus={initialFocusField === "jobOpportunityId"}
          >
            <option value="">Select a job</option>

            {jobs.map((job) => (
              <option key={job.id} value={job.id}>
                {job.companyName
                  ? `${job.companyName} — ${job.positionTitle}`
                  : job.positionTitle}
              </option>
            ))}
          </select>
        </label>

        <label>
          Status
          <select
            name="status"
            value={form.status}
            onChange={handleTextChange}
          >
            {statuses.map((status) => (
              <option
                key={status.value}
                value={status.value}
                disabled={status.disabled}
              >
                {status.label}
              </option>
            ))}
          </select>
        </label>

        <label>
          Résumé version
          <select
            name="resumeMaterialId"
            value={form.resumeMaterialId ?? ""}
            onChange={(event) => setForm((current) => ({...current,
              resumeMaterialId:event.target.value?Number(event.target.value):null}))}
            disabled={resumeSelectionDisabled}
          >
            <option value="">No uploaded résumé selected</option>
            {resumeMaterials.filter(material=>material.active||material.id===form.resumeMaterialId).map(material=><option key={material.id} value={material.id}>{material.displayName}{material.targetJobFamily?` — ${material.targetJobFamily}`:""}{!material.active?" (archived)":""}</option>)}
          </select>
        </label>

        {form.resumeVersion && !form.resumeMaterialId && <p className="field-help">Legacy résumé label: {form.resumeVersion}. Upload and select a real résumé material when ready.</p>}

        <label className="checkbox-field">
          <input
            name="coverLetterNeeded"
            type="checkbox"
            checked={form.coverLetterNeeded}
            onChange={handleCheckboxChange}
          />
          Cover letter needed
        </label>

        <label>
          Portfolio link
          <input
            name="portfolioLink"
            type="url"
            value={form.portfolioLink}
            onChange={handleTextChange}
          />
        </label>

        <label>
          GitHub link
          <input
            name="githubLink"
            type="url"
            value={form.githubLink}
            onChange={handleTextChange}
          />
        </label>

        <label>
          Projects to highlight
          <textarea
            name="projectsToHighlight"
            value={form.projectsToHighlight}
            onChange={handleTextChange}
            placeholder="Career OS, Working Actor OS, DevCommands..."
          />
        </label>

        <label>
          Skills to emphasize
          <textarea
            name="skillsToEmphasize"
            value={form.skillsToEmphasize}
            onChange={handleTextChange}
            placeholder="Java, Spring Boot, React, PostgreSQL..."
          />
        </label>

        <label>
          Interview topics
          <textarea
            name="interviewTopics"
            value={form.interviewTopics}
            onChange={handleTextChange}
            autoFocus={initialFocusField === "interviewTopics"}
          />
        </label>

        <div className="application-form__row">
          <label>
            Recruiter name
            <input
              name="recruiterName"
              value={form.recruiterName}
              onChange={handleTextChange}
              maxLength={200}
            />
          </label>

          <label>
            Recruiter email
            <input
              name="recruiterEmail"
              type="email"
              value={form.recruiterEmail}
              onChange={handleTextChange}
              maxLength={320}
            />
          </label>
        </div>

        <div className="application-form__row">
          <label>
            Application date
            <input
              name="applicationDate"
              type="date"
              value={form.applicationDate}
              onChange={handleTextChange}
            />
          </label>

          <label>
            Follow-up date
            <input
              name="followUpDate"
              type="date"
              value={form.followUpDate}
              onChange={handleTextChange}
              autoFocus={initialFocusField === "followUpDate"}
            />
          </label>
        </div>

        <label>
          Phone screen
          <input
            name="phoneScreenAt"
            type="datetime-local"
            value={form.phoneScreenAt}
            onChange={handleTextChange}
          />
        </label>

        <label>
          Interview 1
          <input
            name="interviewOneAt"
            type="datetime-local"
            value={form.interviewOneAt}
            onChange={handleTextChange}
          />
        </label>

        <label>
          Interview 2
          <input
            name="interviewTwoAt"
            type="datetime-local"
            value={form.interviewTwoAt}
            onChange={handleTextChange}
          />
        </label>

        <label>
          Offer date/time
          <input
            name="offerAt"
            type="datetime-local"
            value={form.offerAt}
            onChange={handleTextChange}
          />
        </label>

        <label>
          Rejected date/time
          <input
            name="rejectedAt"
            type="datetime-local"
            value={form.rejectedAt}
            onChange={handleTextChange}
          />
        </label>

        <label>
          Notes
          <textarea
            name="notes"
            value={form.notes}
            onChange={handleTextChange}
          />
        </label>

        {error && <p role="alert">{error}</p>}

        <div className="application-form__actions">
          <button
            type="submit"
            disabled={
              isSubmitting ||
              form.jobOpportunityId === null
            }
          >
            {isSubmitting ? "Saving..." : submitLabel}
          </button>

          {onCancel && (
            <button
              type="button"
              onClick={onCancel}
              disabled={isSubmitting}
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </section>
  );
}
