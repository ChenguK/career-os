import { useState } from "react";

import ApplicationsPage from "./features/applications/pages/ApplicationsPage";
import CompaniesPage from "./features/companies/pages/CompaniesPage";
import JobsPage from "./features/jobs/pages/JobsPage";

type Page = "applications" | "jobs" | "companies";

export default function App() {
  const [page, setPage] = useState<Page>("applications");

  return (
    <>
      <nav className="app-navigation" aria-label="Main navigation">
        
        <button
          type="button"
          aria-current={
            page === "applications" ? "page" : undefined
          }
          onClick={() => setPage("applications")}
        >
          Applications
        </button>

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
      {page === "applications" && <ApplicationsPage />}
      {page === "jobs" && <JobsPage />}
      {page === "companies" && <CompaniesPage />}
      
    </>
  );
}