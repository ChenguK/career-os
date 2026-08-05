import {
  type ChangeEvent,
  useCallback,
  useEffect,
  useState,
} from "react";

import {
  createCompany,
  deleteCompany,
  getCompanies,
  updateCompany,
} from "../api/companiesApi";
import CompanyForm from "../components/CompanyForm";
import { emptyCompanyInput } from "../types/companyFormDefaults";
import CompanyList from "../components/CompanyList";
import type {
  Company,
  CompanyInput,
} from "../types/company";

function companyToInput(company: Company): CompanyInput {
  return {
    name: company.name,
    websiteUrl: company.websiteUrl ?? "",
    careersUrl: company.careersUrl ?? "",
    industry: company.industry ?? "",
    companyType: company.companyType ?? "",
    mission: company.mission ?? "",
    products: company.products ?? "",
    techStack: company.techStack ?? "",
    remotePolicy: company.remotePolicy ?? "",
    salaryNotes: company.salaryNotes ?? "",
    generalNotes: company.generalNotes ?? "",
    dreamCompany: company.dreamCompany,
  };
}

export default function CompaniesPage() {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [editingCompany, setEditingCompany] =
    useState<Company | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(
    null,
  );
  const [search, setSearch] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadCompanies = useCallback(async (searchTerm: string) => {
    setIsLoading(true);
    setError("");

    try {
      const results = await getCompanies(searchTerm);
      setCompanies(results);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Companies could not be loaded.",
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void loadCompanies(search);
    }, 300);

    return () => window.clearTimeout(timeoutId);
  }, [loadCompanies, search]);

  async function handleCreate(
    input: CompanyInput,
  ): Promise<Company> {
    const company = await createCompany(input);

    setSearch("");
    setMessage(`${company.name} was added.`);
    await loadCompanies("");

    return company;
  }

  async function handleUpdate(
    input: CompanyInput,
  ): Promise<Company> {
    if (!editingCompany) {
      throw new Error("No company has been selected for editing.");
    }

    const company = await updateCompany(
      editingCompany.id,
      input,
    );

    setEditingCompany(null);
    setMessage(`${company.name} was updated.`);
    await loadCompanies(search);

    return company;
  }

  async function handleDelete(company: Company) {
    const confirmed = window.confirm(
      `Delete ${company.name}? This action cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    setDeletingId(company.id);
    setError("");
    setMessage("");

    try {
      await deleteCompany(company.id);

      if (editingCompany?.id === company.id) {
        setEditingCompany(null);
      }

      setMessage(`${company.name} was deleted.`);
      await loadCompanies(search);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "The company could not be deleted.",
      );
    } finally {
      setDeletingId(null);
    }
  }

  function handleEdit(company: Company) {
    setEditingCompany(company);
    setMessage("");
    setError("");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  function handleSearch(event: ChangeEvent<HTMLInputElement>) {
    setSearch(event.target.value);
  }

  return (
    <main className="companies-page">
      <header>
        <p>Career OS</p>
        <h1>Company Playbook</h1>
        <p>
          Research companies, save career information, and prepare
          for future opportunities.
        </p>
      </header>

      {editingCompany ? (
        <CompanyForm
            key={`edit-${editingCompany.id}`}
            heading={`Edit ${editingCompany.name}`}
            submitLabel="Save changes"
            initialValues={companyToInput(editingCompany)}
            onSubmit={handleUpdate}
            onCancel={() => setEditingCompany(null)}
            />
      ) : (
        <CompanyForm
            key="create-company"
            heading="Add company"
            submitLabel="Add company"
            initialValues={emptyCompanyInput}
            onSubmit={handleCreate}
            />
      )}

      {message && (
        <p className="status-message" role="status">
          {message}
        </p>
      )}

      <section aria-labelledby="search-companies-heading">
        <h2 id="search-companies-heading">Search companies</h2>

        <label>
          Search by company name
          <input
            type="search"
            value={search}
            onChange={handleSearch}
            placeholder="Search GitHub, Disney, PostHog..."
          />
        </label>
      </section>

      {isLoading && <p>Loading companies...</p>}

      {error && <p role="alert">{error}</p>}

      {!isLoading && !error && (
        <CompanyList
          companies={companies}
          deletingId={deletingId}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      )}
    </main>
  );
}