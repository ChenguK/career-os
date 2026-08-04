import {
  type ChangeEvent,
  type FormEvent,
  useState,
} from "react";

import type {
  Company,
  CompanyInput,
} from "../types/company";

interface CompanyFormProps {
  onCreate: (input: CompanyInput) => Promise<Company>;
}

const initialForm: CompanyInput = {
  name: "",
  websiteUrl: "",
  careersUrl: "",
  industry: "",
  companyType: "",
  mission: "",
  products: "",
  techStack: "",
  remotePolicy: "",
  salaryNotes: "",
  generalNotes: "",
  dreamCompany: false,
};

export default function CompanyForm({
  onCreate,
}: CompanyFormProps) {
  const [form, setForm] = useState<CompanyInput>(initialForm);
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
      await onCreate(form);
      setForm(initialForm);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "The company could not be created.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section aria-labelledby="add-company-heading">
      <h2 id="add-company-heading">Add company</h2>

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

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : "Add company"}
        </button>
      </form>
    </section>
  );
}