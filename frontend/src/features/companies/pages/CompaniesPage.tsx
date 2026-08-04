import {
  type ChangeEvent,
  useCallback,
  useEffect,
  useState,
} from "react";

import {
  createCompany,
  getCompanies,
} from "../api/companiesApi";
import CompanyForm from "../components/CompanyForm";
import CompanyList from "../components/CompanyList";
import type {
  Company,
  CompanyInput,
} from "../types/company";

export default function CompaniesPage() {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [search, setSearch] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

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
    await loadCompanies("");

    return company;
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

      <CompanyForm onCreate={handleCreate} />

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
        <CompanyList companies={companies} />
      )}
    </main>
  );
}