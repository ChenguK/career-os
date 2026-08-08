import {
  type ChangeEvent,
  type SyntheticEvent,
  useState,
} from "react";

import type { Company } from "../../companies/types/company";
import type {
  JobOpportunity,
  JobOpportunityInput,
} from "../types/job";
import { emptyJobInput } from "../types/jobFormDefaults";

interface JobFormProps {
  heading: string;
  submitLabel: string;
  companies: Company[];
  initialValues?: JobOpportunityInput;
  onSubmit: (
    input: JobOpportunityInput,
  ) => Promise<JobOpportunity>;
  onCancel?: () => void;
}

export default function JobForm({
  heading,
  submitLabel,
  companies,
  initialValues = emptyJobInput,
  onSubmit,
  onCancel,
}: JobFormProps) {
  const [form, setForm] =
    useState<JobOpportunityInput>(initialValues);
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

  function handleCompanyChange(
    event: ChangeEvent<HTMLSelectElement>,
  ) {
    const value = event.target.value;

    setForm((current) => ({
      ...current,
      companyId: value ? Number(value) : null,
    }));
  }

  function handleNumberChange(
    event: ChangeEvent<HTMLInputElement>,
  ) {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value === "" ? null : Number(value),
    }));
  }

  async function handleSubmit(
    event: SyntheticEvent<HTMLFormElement, SubmitEvent>,
  ) {
    event.preventDefault();

    setError("");
    setIsSubmitting(true);

    try {
      await onSubmit(form);

      if (!initialValues.positionTitle) {
        setForm(emptyJobInput);
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "The job could not be saved.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section aria-labelledby={`${heading}-heading`}>
      <h2 id={`${heading}-heading`}>{heading}</h2>

      <form className="job-form" onSubmit={handleSubmit}>
        <label>
          Company
          <select
            name="companyId"
            value={form.companyId ?? ""}
            onChange={handleCompanyChange}
          >
            <option value="">No linked company</option>

            {companies.map((company) => (
              <option key={company.id} value={company.id}>
                {company.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          Position title
          <input
            name="positionTitle"
            value={form.positionTitle}
            onChange={handleTextChange}
            required
            maxLength={200}
          />
        </label>

        <label>
          Department
          <input
            name="department"
            value={form.department}
            onChange={handleTextChange}
            maxLength={150}
          />
        </label>

        <label>
          Location
          <input
            name="location"
            value={form.location}
            onChange={handleTextChange}
            maxLength={200}
          />
        </label>

        <label>
          Work arrangement
          <select
            name="remoteType"
            value={form.remoteType}
            onChange={handleTextChange}
          >
            <option value="UNKNOWN">Unknown</option>
            <option value="REMOTE">Remote</option>
            <option value="HYBRID">Hybrid</option>
            <option value="ONSITE">Onsite</option>
          </select>
        </label>

        <label>
          Employment type
          <input
            name="employmentType"
            value={form.employmentType}
            onChange={handleTextChange}
            placeholder="Full-time, contract, internship..."
            maxLength={50}
          />
        </label>

        <div className="job-form__row">
          <label>
            Minimum salary
            <input
              name="salaryMin"
              type="number"
              min="0"
              step="0.01"
              value={form.salaryMin ?? ""}
              onChange={handleNumberChange}
            />
          </label>

          <label>
            Maximum salary
            <input
              name="salaryMax"
              type="number"
              min="0"
              step="0.01"
              value={form.salaryMax ?? ""}
              onChange={handleNumberChange}
            />
          </label>

          <label>
            Currency
            <input
              name="salaryCurrency"
              value={form.salaryCurrency}
              onChange={handleTextChange}
              minLength={3}
              maxLength={3}
            />
          </label>
        </div>

        <label>
          Salary notes
          <textarea
            name="salaryNotes"
            value={form.salaryNotes}
            onChange={handleTextChange}
          />
        </label>

        <label>
          Application URL
          <input
            name="applicationUrl"
            type="url"
            value={form.applicationUrl}
            onChange={handleTextChange}
          />
        </label>

        <label>
          Source
          <input
            name="source"
            value={form.source}
            onChange={handleTextChange}
            placeholder="Company site, LinkedIn, referral..."
            maxLength={150}
          />
        </label>

        <div className="job-form__row">
          <label>
            Date posted
            <input
              name="datePosted"
              type="date"
              value={form.datePosted}
              onChange={handleTextChange}
            />
          </label>

          <label>
            Closing date
            <input
              name="closingDate"
              type="date"
              value={form.closingDate}
              onChange={handleTextChange}
            />
          </label>
        </div>

        <div className="job-form__row">
          <label>
            Priority
            <input
              name="priority"
              type="number"
              min="1"
              max="5"
              value={form.priority ?? ""}
              onChange={handleNumberChange}
            />
          </label>

          <label>
            Match score
            <input
              name="matchScore"
              type="number"
              min="0"
              max="10"
              step="0.1"
              value={form.matchScore ?? ""}
              onChange={handleNumberChange}
            />
          </label>
        </div>

        <label>
          Job description
          <textarea
            name="jobDescription"
            value={form.jobDescription}
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

        <div className="job-form__actions">
          <button type="submit" disabled={isSubmitting}>
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