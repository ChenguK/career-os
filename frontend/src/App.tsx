import { useState } from "react";

import CompaniesPage from "./features/companies/pages/CompaniesPage";
import JobsPage from "./features/jobs/pages/JobsPage";

type Page = "companies" | "jobs";

export default function App() {
  const [page, setPage] = useState<Page>("jobs");

  return (
    <>
      <nav className="app-navigation" aria-label="Main navigation">
        <button
          type="button"
          aria-current={page === "jobs" ? "page" : undefined}
          onClick={() => setPage("jobs")}
        >
          Jobs
        </button>

        <button
          type="button"
          aria-current={
            page === "companies" ? "page" : undefined
          }
          onClick={() => setPage("companies")}
        >
          Companies
        </button>
      </nav>

      {page === "jobs" ? <JobsPage /> : <CompaniesPage />}
    </>
  );
}