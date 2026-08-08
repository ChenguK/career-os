import {
  type ChangeEvent,
  useCallback,
  useEffect,
  useState,
} from "react";

import { getCompanies } from "../../companies/api/companiesApi";
import type { Company } from "../../companies/types/company";
import {
  createJob,
  deleteJob,
  getJobs,
  updateJob,
} from "../api/jobsApi";
import { emptyJobInput } from "../types/jobFormDefaults";
import JobForm from "../components/JobForm";
import JobList from "../components/JobList";
import { useDebouncedValue } from "../../../shared/hooks/useDebouncedValue";
import type {
  JobOpportunity,
  JobOpportunityInput,
} from "../types/job";

export default function JobsPage() {
  const [jobs, setJobs] = useState<JobOpportunity[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [search, setSearch] = useState("");
  const debouncedSearch = useDebouncedValue(search, 300);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [editingJob, setEditingJob] = useState<JobOpportunity | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const loadJobs = useCallback(async (searchTerm: string) => {
    setIsLoading(true);
    setError("");

    try {
      const results = await getJobs(searchTerm);
      setJobs(results);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Jobs could not be loaded.",
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void getCompanies()
      .then(setCompanies)
      .catch(() => {
        setError("Companies could not be loaded.");
      });
  }, []);

  useEffect(() => {
  let isCancelled = false;

  async function fetchJobs() {
    setIsLoading(true);
    setError("");

    try {
      const results = await getJobs(debouncedSearch);

      if (!isCancelled) {
        setJobs(results);
      }
    } catch (caughtError) {
      if (!isCancelled) {
        setError(
          caughtError instanceof Error
            ? caughtError.message
            : "Jobs could not be loaded.",
        );
      }
    } finally {
      if (!isCancelled) {
        setIsLoading(false);
      }
    }
  }

  void fetchJobs();

    return () => {
      isCancelled = true;
    };
  }, [debouncedSearch]);

  async function handleCreate(
    input: JobOpportunityInput,
  ): Promise<JobOpportunity> {
    const job = await createJob(input);

    setSearch("");
    setMessage(`${job.positionTitle} was added.`);
    await loadJobs("");

    return job;
  }

  function handleSearch(event: ChangeEvent<HTMLInputElement>) {
    setSearch(event.target.value);
  }

  function jobToInput(
  job: JobOpportunity,
    ): JobOpportunityInput {
    return {
        companyId: job.companyId,
        positionTitle: job.positionTitle,
        department: job.department ?? "",
        location: job.location ?? "",
        remoteType: job.remoteType,
        employmentType: job.employmentType ?? "",
        salaryMin: job.salaryMin,
        salaryMax: job.salaryMax,
        salaryCurrency: job.salaryCurrency,
        salaryNotes: job.salaryNotes ?? "",
        applicationUrl: job.applicationUrl ?? "",
        source: job.source ?? "",
        datePosted: job.datePosted ?? "",
        closingDate: job.closingDate ?? "",
        priority: job.priority,
        matchScore: job.matchScore,
        jobDescription: job.jobDescription ?? "",
        notes: job.notes ?? "",
    };
    }
    async function handleUpdate(
        input: JobOpportunityInput,
        ): Promise<JobOpportunity> {
        if (!editingJob) {
            throw new Error(
            "No job has been selected for editing.",
            );
        }

        const job = await updateJob(editingJob.id, input);

        setEditingJob(null);
        setMessage(`${job.positionTitle} was updated.`);
        await loadJobs(search);

        return job;
        }

    async function handleDelete(job: JobOpportunity) {
        const confirmed = window.confirm(
            `Delete ${job.positionTitle}? This action cannot be undone.`,
        );

        if (!confirmed) {
            return;
        }

        setDeletingId(job.id);
        setError("");
        setMessage("");

        try {
            await deleteJob(job.id);

            if (editingJob?.id === job.id) {
            setEditingJob(null);
            }

            setMessage(`${job.positionTitle} was deleted.`);
            await loadJobs(search);
        } catch (caughtError) {
            setError(
            caughtError instanceof Error
                ? caughtError.message
                : "The job could not be deleted.",
            );
        } finally {
            setDeletingId(null);
        }
        }

        function handleEdit(job: JobOpportunity) {
            setEditingJob(job);
            setMessage("");
            setError("");

            window.scrollTo({
                top: 0,
                behavior: "smooth",
            });
            }

  return (
    <main className="jobs-page">
      <header>
        <p>Career OS</p>
        <h1>Job Tracker</h1>
        <p>
          Save opportunities, prioritize applications, and track
          the roles that best match your goals.
        </p>
      </header>

      {editingJob ? (
        <JobForm
            key={`edit-job-${editingJob.id}`}
            heading={`Edit ${editingJob.positionTitle}`}
            submitLabel="Save changes"
            companies={companies}
            initialValues={jobToInput(editingJob)}
            onSubmit={handleUpdate}
            onCancel={() => setEditingJob(null)}
        />
        ) : (
        <JobForm
            key="create-job"
            heading="Add job"
            submitLabel="Add job"
            companies={companies}
            initialValues={emptyJobInput}
            onSubmit={handleCreate}
        />
        )}

      {message && (
        <p className="status-message" role="status">
          {message}
        </p>
      )}

      <section aria-labelledby="search-jobs-heading">
        <h2 id="search-jobs-heading">Search jobs</h2>

        <label>
          Search by position title
          <input
            type="search"
            value={search}
            onChange={handleSearch}
            placeholder="Search engineer, administrator..."
          />
        </label>
      </section>

      {isLoading && <p>Loading jobs...</p>}

      {error && <p role="alert">{error}</p>}

      {!isLoading && !error && 
        <JobList
            jobs={jobs}
            deletingId={deletingId}
            onEdit={handleEdit}
            onDelete={handleDelete}
            />}
    </main>
  );
}