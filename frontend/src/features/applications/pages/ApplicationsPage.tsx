import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";

import { getJobs } from "../../jobs/api/jobsApi";
import type { JobOpportunity } from "../../jobs/types/job";
import {
  createApplication,
  deleteApplication,
  getApplicationTracker,
  getApplications,
  updateApplication,
} from "../api/applicationsApi";
import ApplicationForm from "../components/ApplicationForm";
import ApplicationList from "../components/ApplicationList";
import ImportJobsPanel from "../components/ImportJobsPanel";
import type {
  Application,
  ApplicationInput,
  ApplicationStatus,
} from "../types/application";
import type { ApplicationTrackerRow } from "../types/applicationTracker";
import { emptyApplicationInput } from "../types/applicationFormDefaults";
import { useDebouncedValue } from "../../../shared/hooks/useDebouncedValue";
import type {
  ApplicationTrackerQuery,
  ApplicationTrackerSort,
} from "../types/applicationTracker";
import type { RemoteType } from "../../jobs/types/job";

function toLocalDateTimeInput(
  value: string | null,
): string {
  if (!value) {
    return "";
  }

  const date = new Date(value);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");

  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function applicationToInput(
  application: Application,
): ApplicationInput {
  return {
    jobOpportunityId: application.jobOpportunityId,
    status: application.status,
    resumeVersion: application.resumeVersion ?? "",
    coverLetterNeeded: application.coverLetterNeeded,
    portfolioLink: application.portfolioLink ?? "",
    githubLink: application.githubLink ?? "",
    projectsToHighlight:
      application.projectsToHighlight ?? "",
    skillsToEmphasize:
      application.skillsToEmphasize ?? "",
    interviewTopics: application.interviewTopics ?? "",
    recruiterName: application.recruiterName ?? "",
    recruiterEmail: application.recruiterEmail ?? "",
    applicationDate: application.applicationDate ?? "",
    followUpDate: application.followUpDate ?? "",
    phoneScreenAt: toLocalDateTimeInput(
      application.phoneScreenAt,
    ),
    interviewOneAt: toLocalDateTimeInput(
      application.interviewOneAt,
    ),
    interviewTwoAt: toLocalDateTimeInput(
      application.interviewTwoAt,
    ),
    offerAt: toLocalDateTimeInput(application.offerAt),
    rejectedAt: toLocalDateTimeInput(
      application.rejectedAt,
    ),
    notes: application.notes ?? "",
  };
}

export default function ApplicationsPage() {
  const [applications, setApplications] = useState<
    Application[]
  >([]);

  const [jobs, setJobs] = useState<JobOpportunity[]>([]);
  const [trackerRows, setTrackerRows] = useState<
    ApplicationTrackerRow[]
  >([]);
  const [trackerPage, setTrackerPage] = useState(0);
  const [trackerTotalRows, setTrackerTotalRows] = useState(0);
  const [trackerTotalPages, setTrackerTotalPages] = useState(0);

  const [search, setSearch] = useState("");
  const debouncedSearch = useDebouncedValue(search, 300);
  const [statusFilter, setStatusFilter] = useState("");
  const [priorityFilter, setPriorityFilter] = useState("");
  const [remoteTypeFilter, setRemoteTypeFilter] = useState("");
  const [companyFilter, setCompanyFilter] = useState("");
  const [sortField, setSortField] =
    useState<ApplicationTrackerSort>("priority");
  const [sortDirection, setSortDirection] =
    useState<"asc" | "desc">("asc");

  const [editingApplication, setEditingApplication] =
    useState<Application | null>(null);
  const [creatingForJobId, setCreatingForJobId] =
    useState<number | null>(null);

  const [deletingId, setDeletingId] =
    useState<number | null>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [isTrackerLoading, setIsTrackerLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [isImportOpen, setIsImportOpen] = useState(false);

  const loadApplications = useCallback(async () => {
    setIsLoading(true);
    setError("");

    try {
      const results = await getApplications();
      setApplications(results);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Applications could not be loaded.",
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  const loadJobs = useCallback(async () => {
    try {
      const results = await getJobs();
      setJobs(results);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Jobs could not be loaded.",
      );
    }
  }, []);

  const loadTracker = useCallback(async (
    query: ApplicationTrackerQuery,
  ) => {
    setIsTrackerLoading(true);
    try {
      const result = await getApplicationTracker(query);
      setTrackerRows(result.content);
      setTrackerPage(result.page);
      setTrackerTotalRows(result.totalRows);
      setTrackerTotalPages(result.totalPages);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Tracker data could not be loaded.",
      );
    } finally {
      setIsTrackerLoading(false);
    }
  }, []);

  useEffect(() => {
    let isCancelled = false;

    async function loadPage() {
      setIsLoading(true);
      setError("");

      try {
        const [applicationResults, jobResults] =
          await Promise.all([
            getApplications(),
            getJobs(),
          ]);

        if (!isCancelled) {
          setApplications(applicationResults);
          setJobs(jobResults);
        }
      } catch (caughtError) {
        if (!isCancelled) {
          setError(
            caughtError instanceof Error
              ? caughtError.message
              : "Application data could not be loaded.",
          );
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadPage();

    return () => {
      isCancelled = true;
    };
  }, []);

  const availableJobs = useMemo(
    () =>
      jobs.filter(
        (job) =>
          !applications.some(
            (application) =>
              application.jobOpportunityId === job.id,
          ),
      ),
    [applications, jobs],
  );

  const companyOptions = useMemo(() => {
    const companies = new Map<number, string>();

    jobs.forEach((job) => {
      if (job.companyId !== null && job.companyName) {
        companies.set(job.companyId, job.companyName);
      }
    });

    return [...companies.entries()].sort((first, second) =>
      first[1].localeCompare(second[1]),
    );
  }, [jobs]);

  const trackerQuery = useMemo<ApplicationTrackerQuery>(
    () => ({
      search: debouncedSearch,
      statuses: statusFilter
        ? [statusFilter as ApplicationStatus]
        : undefined,
      priorities: priorityFilter
        ? [Number(priorityFilter)]
        : undefined,
      remoteTypes: remoteTypeFilter
        ? [remoteTypeFilter as RemoteType]
        : undefined,
      companyId: companyFilter
        ? Number(companyFilter)
        : undefined,
      sort: sortField,
      direction: sortDirection,
      page: trackerPage,
      size: 25,
    }),
    [
      companyFilter,
      debouncedSearch,
      priorityFilter,
      remoteTypeFilter,
      sortDirection,
      sortField,
      statusFilter,
      trackerPage,
    ],
  );

  const hasActiveCriteria = Boolean(
    search.trim() ||
    statusFilter ||
    priorityFilter ||
    remoteTypeFilter ||
    companyFilter ||
    sortField !== "priority" ||
    sortDirection !== "asc",
  );

  useEffect(() => {
    let isCancelled = false;

    async function loadTrackerPage() {
      setIsTrackerLoading(true);

      try {
        const result = await getApplicationTracker(trackerQuery);

        if (!isCancelled) {
          setTrackerRows(result.content);
          setTrackerPage(result.page);
          setTrackerTotalRows(result.totalRows);
          setTrackerTotalPages(result.totalPages);
        }
      } catch (caughtError) {
        if (!isCancelled) {
          setError(
            caughtError instanceof Error
              ? caughtError.message
              : "Tracker data could not be loaded.",
          );
        }
      } finally {
        if (!isCancelled) {
          setIsTrackerLoading(false);
        }
      }
    }

    void loadTrackerPage();

    return () => {
      isCancelled = true;
    };
  }, [trackerQuery]);

  function resetPage() {
    setTrackerPage(0);
  }

  function clearFilters() {
    setSearch("");
    setStatusFilter("");
    setPriorityFilter("");
    setRemoteTypeFilter("");
    setCompanyFilter("");
    setSortField("priority");
    setSortDirection("asc");
    setTrackerPage(0);
  }

  async function handleCreate(
    input: ApplicationInput,
  ): Promise<Application> {
    const application =
      await createApplication(input);

    setMessage(
      `${application.positionTitle} application was added.`,
    );
    setCreatingForJobId(null);

    await Promise.all([
      loadApplications(),
      loadJobs(),
      loadTracker(trackerQuery),
    ]);

    return application;
  }

  async function handleUpdate(
    input: ApplicationInput,
  ): Promise<Application> {
    if (!editingApplication) {
      throw new Error(
        "No application has been selected for editing.",
      );
    }

    const application = await updateApplication(
      editingApplication.id,
      input,
    );

    setEditingApplication(null);

    setMessage(
      `${application.positionTitle} application was updated.`,
    );

    await Promise.all([
      loadApplications(),
      loadTracker(trackerQuery),
    ]);

    return application;
  }

  async function handleDelete(applicationId: number) {
    const application = applications.find(
      (candidate) => candidate.id === applicationId,
    );

    if (!application) {
      setError("The selected application could not be found.");
      return;
    }

    const confirmed = window.confirm(
      `Delete the application for ${application.positionTitle}? This action cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    setDeletingId(application.id);
    setError("");
    setMessage("");

    try {
      await deleteApplication(application.id);

      if (
        editingApplication?.id === application.id
      ) {
        setEditingApplication(null);
      }

      setMessage(
        `${application.positionTitle} application was deleted.`,
      );

      await Promise.all([
        loadApplications(),
        loadJobs(),
        loadTracker(trackerQuery),
      ]);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "The application could not be deleted.",
      );
    } finally {
      setDeletingId(null);
    }
  }

  function handleEdit(applicationId: number) {
    const application = applications.find(
      (candidate) => candidate.id === applicationId,
    );

    if (!application) {
      setError("The selected application could not be found.");
      return;
    }

    setEditingApplication(application);
    setCreatingForJobId(null);
    setMessage("");
    setError("");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  function handleAddApplication(jobOpportunityId: number) {
    setEditingApplication(null);
    setCreatingForJobId(jobOpportunityId);
    setMessage("");
    setError("");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  return (
    <main className="applications-page">
      <header>
        <p>Career OS</p>
        <h1>Application Tracker</h1>
        <p>
          Track application status, follow-ups, recruiter
          information, résumé strategy, and interview progress.
        </p>
        <button type="button" onClick={() => setIsImportOpen(true)}>
          Import Jobs
        </button>
      </header>

      {isImportOpen && (
        <ImportJobsPanel
          onClose={() => setIsImportOpen(false)}
          onImportComplete={() => loadTracker(trackerQuery)}
        />
      )}

      {editingApplication ? (
        <ApplicationForm
          key={`edit-application-${editingApplication.id}`}
          heading={`Edit ${editingApplication.positionTitle}`}
          submitLabel="Save changes"
          jobs={jobs}
          initialValues={applicationToInput(
            editingApplication,
          )}
          isEditing
          onSubmit={handleUpdate}
          onCancel={() =>
            setEditingApplication(null)
          }
        />
      ) : (
        <ApplicationForm
          key={`create-application-${creatingForJobId ?? "new"}`}
          heading="Add application"
          submitLabel="Add application"
          jobs={availableJobs}
          initialValues={{
            ...emptyApplicationInput,
            jobOpportunityId: creatingForJobId,
          }}
          onSubmit={handleCreate}
        />
      )}

      {message && (
        <p
          className="status-message"
          role="status"
        >
          {message}
        </p>
      )}

      {!editingApplication &&
        availableJobs.length === 0 &&
        jobs.length > 0 && (
          <p>
            Every saved job already has an application
            record.
          </p>
        )}

      {jobs.length === 0 && !isLoading && (
        <p>
          Add a job before creating an application.
        </p>
      )}

      <section
        className="tracker-controls"
        aria-labelledby="tracker-controls-heading"
      >
        <h2 id="tracker-controls-heading">Tracker view</h2>

        <div className="tracker-controls__grid">
          <label>
            Search
            <input
              type="search"
              value={search}
              placeholder="Company, title, location, or source"
              onChange={(event) => {
                setSearch(event.target.value);
                resetPage();
              }}
            />
          </label>

          <label>
            Status filter
            <select
              value={statusFilter}
              onChange={(event) => {
                setStatusFilter(event.target.value);
                resetPage();
              }}
            >
              <option value="">All statuses</option>
              <option value="SAVED">Saved</option>
              <option value="PREPARING">Preparing</option>
              <option value="APPLIED">Applied</option>
              <option value="PHONE_SCREEN">Phone Screen</option>
              <option value="INTERVIEW_ONE">Interview 1</option>
              <option value="INTERVIEW_TWO">Interview 2</option>
              <option value="OFFER">Offer</option>
              <option value="REJECTED">Rejected</option>
              <option value="WITHDRAWN">Withdrawn</option>
              <option value="CLOSED">Closed</option>
            </select>
          </label>

          <label>
            Priority filter
            <select
              value={priorityFilter}
              onChange={(event) => {
                setPriorityFilter(event.target.value);
                resetPage();
              }}
            >
              <option value="">All priorities</option>
              {[1, 2, 3, 4, 5].map((priority) => (
                <option key={priority} value={priority}>
                  {priority}
                </option>
              ))}
            </select>
          </label>

          <label>
            Work Arrangement filter
            <select
              value={remoteTypeFilter}
              onChange={(event) => {
                setRemoteTypeFilter(event.target.value);
                resetPage();
              }}
            >
              <option value="">All arrangements</option>
              <option value="REMOTE">Remote</option>
              <option value="HYBRID">Hybrid</option>
              <option value="ONSITE">Onsite</option>
              <option value="UNKNOWN">Unknown</option>
            </select>
          </label>

          <label>
            Company filter
            <select
              value={companyFilter}
              onChange={(event) => {
                setCompanyFilter(event.target.value);
                resetPage();
              }}
            >
              <option value="">All companies</option>
              {companyOptions.map(([id, name]) => (
                <option key={id} value={id}>
                  {name}
                </option>
              ))}
            </select>
          </label>

          <label>
            Sort by
            <select
              value={sortField}
              onChange={(event) => {
                setSortField(
                  event.target.value as ApplicationTrackerSort,
                );
                resetPage();
              }}
            >
              <option value="priority">Priority</option>
              <option value="company">Company</option>
              <option value="positionTitle">Position Title</option>
              <option value="status">Status</option>
              <option value="matchScore">Match Score</option>
              <option value="location">Location</option>
              <option value="remoteType">Work Arrangement</option>
              <option value="salaryMin">Minimum Salary</option>
              <option value="datePosted">Date Posted</option>
              <option value="applicationDate">Application Date</option>
              <option value="followUpDate">Follow-Up Date</option>
              <option value="source">Source</option>
              <option value="createdAt">Date Added</option>
            </select>
          </label>

          <label>
            Direction
            <select
              value={sortDirection}
              onChange={(event) => {
                setSortDirection(
                  event.target.value as "asc" | "desc",
                );
                resetPage();
              }}
            >
              <option value="asc">Ascending</option>
              <option value="desc">Descending</option>
            </select>
          </label>
        </div>

        <button type="button" onClick={clearFilters}>
          Clear filters
        </button>
      </section>

      {(isLoading || isTrackerLoading) && (
        <p>Loading applications...</p>
      )}

      {error && <p role="alert">{error}</p>}

      {!isLoading && !isTrackerLoading && !error &&
        trackerRows.length === 0 && hasActiveCriteria && (
          <section aria-label="Filtered tracker results">
            <p>No records match the current filters.</p>
            <button type="button" onClick={clearFilters}>
              Clear filters
            </button>
          </section>
        )}

      {!isLoading && !isTrackerLoading && !error &&
        !(trackerRows.length === 0 && hasActiveCriteria) && (
        <ApplicationList
          rows={trackerRows}
          deletingId={deletingId}
          onEdit={handleEdit}
          onDelete={handleDelete}
          onAddApplication={handleAddApplication}
        />
      )}

      {!isLoading && !isTrackerLoading && !error &&
        trackerTotalRows > 0 && (
          <nav
            className="tracker-pagination"
            aria-label="Tracker pagination"
          >
            <button
              type="button"
              disabled={trackerPage === 0}
              onClick={() =>
                setTrackerPage((current) => Math.max(0, current - 1))
              }
            >
              Previous
            </button>
            <span>
              Page {trackerPage + 1} of {trackerTotalPages}
            </span>
            <span>
              {trackerTotalRows} result
              {trackerTotalRows === 1 ? "" : "s"}
            </span>
            <button
              type="button"
              disabled={trackerPage + 1 >= trackerTotalPages}
              onClick={() =>
                setTrackerPage((current) => current + 1)
              }
            >
              Next
            </button>
          </nav>
        )}
    </main>
  );
}
