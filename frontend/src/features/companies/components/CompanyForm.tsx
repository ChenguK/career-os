import {
  type ChangeEvent,
  type FormEvent,
  useState,
} from "react";

import type {
  Company,
  CompanyInput,
} from "../types/company";
import { emptyCompanyInput } from "../types/companyFormDefaults";

interface CompanyFormProps {
  heading: string;
  submitLabel: string;
  initialValues?: CompanyInput;
  onSubmit: (input: CompanyInput) => Promise<Company>;
  onCancel?: () => void;
}

export default function CompanyForm({
  heading,
  submitLabel,
  initialValues = emptyCompanyInput,
  onSubmit,
  onCancel,
}: CompanyFormProps) {
  const [form, setForm] = useState<CompanyInput>(initialValues);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  function handleChange(
    event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) {
    const { name, value, type } = event.target;

    const nextValue =
      type === "checkbox"
        ? (event.target as HTMLInputElement).checked
        : value;

    setForm((current) => ({
      ...current,
      [name]: nextValue,
    }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setIsSubmitting(true);

    try {
      await onSubmit(form);

      if (!initialValues.name) {
        setForm(emptyCompanyInput);
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "The company could not be saved.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section aria-labelledby={`${heading}-heading`}>
      <h2 id={`${heading}-heading`}>{heading}</h2>

      <form className="company-form" onSubmit={handleSubmit}>
        <label>
          Company name
          <input
            name="name"
            value={form.name}
            onChange={handleChange}
            required
            maxLength={200}
          />
        </label>

        <label>
          Website
          <input
            name="websiteUrl"
            type="url"
            value={form.websiteUrl}
            onChange={handleChange}
          />
        </label>

        <label>
          Careers page
          <input
            name="careersUrl"
            type="url"
            value={form.careersUrl}
            onChange={handleChange}
          />
        </label>

        <label>
          Industry
          <input
            name="industry"
            value={form.industry}
            onChange={handleChange}
            maxLength={150}
          />
        </label>

        <label>
          Company type
          <input
            name="companyType"
            value={form.companyType}
            onChange={handleChange}
            maxLength={100}
          />
        </label>

        <label>
          Mission
          <textarea
            name="mission"
            value={form.mission}
            onChange={handleChange}
          />
        </label>

        <label>
          Products
          <textarea
            name="products"
            value={form.products}
            onChange={handleChange}
          />
        </label>

        <label>
          Tech stack
          <textarea
            name="techStack"
            value={form.techStack}
            onChange={handleChange}
          />
        </label>

        <label>
          Remote policy
          <textarea
            name="remotePolicy"
            value={form.remotePolicy}
            onChange={handleChange}
          />
        </label>

        <label>
          Salary notes
          <textarea
            name="salaryNotes"
            value={form.salaryNotes}
            onChange={handleChange}
          />
        </label>

        <label>
          General notes
          <textarea
            name="generalNotes"
            value={form.generalNotes}
            onChange={handleChange}
          />
        </label>

        <label className="checkbox-field">
          <input
            name="dreamCompany"
            type="checkbox"
            checked={form.dreamCompany}
            onChange={handleChange}
          />
          Dream company
        </label>

        {error && <p role="alert">{error}</p>}

        <div className="company-form__actions">
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