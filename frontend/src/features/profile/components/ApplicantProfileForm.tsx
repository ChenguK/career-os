import {
  type ChangeEvent,
  type FormEvent,
  useState,
} from "react";

import type {
  ApplicantProfile,
  ApplicantProfileInput,
} from "../types/applicantProfile";

interface ApplicantProfileFormProps {
  initialValues: ApplicantProfileInput;
  onSubmit: (input: ApplicantProfileInput) => Promise<ApplicantProfile>;
}

export default function ApplicantProfileForm({
  initialValues,
  onSubmit,
}: ApplicantProfileFormProps) {
  const [form, setForm] = useState(initialValues);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  function handleChange(
    event: ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) {
    const { name, value } = event.target;
    setForm((current) => ({
      ...current,
      [name]: name === "minimumSalary"
        ? value === "" ? null : Number(value)
        : value,
    }));
  }

  function handleNullableBoolean(
    event: ChangeEvent<HTMLSelectElement>,
  ) {
    const value = event.target.value;
    setForm((current) => ({
      ...current,
      [event.target.name]: value === "" ? null : value === "true",
    }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);
    try {
      await onSubmit(form);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Applicant profile could not be saved.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="applicant-profile-form" onSubmit={handleSubmit}>
      <fieldset>
        <legend>Contact</legend>
        <div className="applicant-profile-form__grid">
          <label>First name
            <input name="firstName" value={form.firstName}
              onChange={handleChange} required maxLength={100} />
          </label>
          <label>Last name
            <input name="lastName" value={form.lastName}
              onChange={handleChange} required maxLength={100} />
          </label>
          <label>Preferred name
            <input name="preferredName" value={form.preferredName}
              onChange={handleChange} maxLength={100} />
          </label>
          <label>Email
            <input name="email" type="email" value={form.email}
              onChange={handleChange} required maxLength={320} />
          </label>
          <label>Phone
            <input name="phone" type="tel" value={form.phone}
              onChange={handleChange} maxLength={50} />
          </label>
          <label>City
            <input name="city" value={form.city}
              onChange={handleChange} maxLength={100} />
          </label>
          <label>State or region
            <input name="stateRegion" value={form.stateRegion}
              onChange={handleChange} maxLength={100} />
          </label>
          <label>Country
            <input name="country" value={form.country}
              onChange={handleChange} maxLength={100} />
          </label>
          <label>Postal code
            <input name="postalCode" value={form.postalCode}
              onChange={handleChange} maxLength={20} />
          </label>
        </div>
      </fieldset>

      <fieldset>
        <legend>Professional Links</legend>
        <div className="applicant-profile-form__grid">
          <label>Portfolio URL
            <input name="portfolioUrl" type="url" value={form.portfolioUrl}
              onChange={handleChange} maxLength={1000} />
          </label>
          <label>GitHub URL
            <input name="githubUrl" type="url" value={form.githubUrl}
              onChange={handleChange} maxLength={1000} />
          </label>
          <label>LinkedIn URL
            <input name="linkedinUrl" type="url" value={form.linkedinUrl}
              onChange={handleChange} maxLength={1000} />
          </label>
        </div>
      </fieldset>

      <fieldset>
        <legend>Work Preferences</legend>
        <div className="applicant-profile-form__grid">
          <label>Preferred work arrangement
            <select name="preferredWorkArrangement"
              value={form.preferredWorkArrangement} onChange={handleChange}>
              <option value="UNKNOWN">No preference recorded</option>
              <option value="REMOTE">Remote</option>
              <option value="HYBRID">Hybrid</option>
              <option value="ONSITE">Onsite</option>
            </select>
          </label>
          <label>Minimum salary
            <input name="minimumSalary" type="number" min="0" step="0.01"
              value={form.minimumSalary ?? ""} onChange={handleChange} />
          </label>
          <label>Salary currency
            <input name="salaryCurrency" value={form.salaryCurrency}
              onChange={handleChange} required maxLength={3}
              pattern="[A-Za-z]{3}" />
          </label>
          <label>Willing to relocate
            <select name="willingToRelocate"
              value={form.willingToRelocate === null
                ? "" : String(form.willingToRelocate)}
              onChange={handleNullableBoolean}>
              <option value="">Not specified</option>
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          </label>
          <label>Willing to travel
            <select name="willingToTravel"
              value={form.willingToTravel === null
                ? "" : String(form.willingToTravel)}
              onChange={handleNullableBoolean}>
              <option value="">Not specified</option>
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          </label>
        </div>
      </fieldset>

      <fieldset>
        <legend>Application Defaults</legend>
        <label>Legacy default résumé label
          <input name="defaultResumeVersion"
            value={form.defaultResumeVersion} onChange={handleChange}
            maxLength={100} />
        </label>
        <p className="field-help">Kept for existing records and spreadsheet compatibility. Choose the real default résumé in Career Materials.</p>
      </fieldset>

      {error && <p role="alert">{error}</p>}
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Saving…" : "Save profile"}
      </button>
    </form>
  );
}
