import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { getJobs } from "../../jobs/api/jobsApi";
import { updateJob } from "../../jobs/api/jobsApi";
import type {
  JobOpportunity,
  JobOpportunityInput,
} from "../../jobs/types/job";
import { getCompanies } from "../../companies/api/companiesApi";
import type { Company } from "../../companies/types/company";
import {
  createApplication,
  deleteApplication,
  exportApplicationTrackerCsv,
  exportApplicationTrackerXlsx,
  getApplicationTracker,
  getApplicationTrackerRow,
  getApplications,
  updateApplication,
} from "../api/applicationsApi";
import ApplicationForm from "../components/ApplicationForm";
import ApplicationList from "../components/ApplicationList";
import ImportJobsPanel from "../components/ImportJobsPanel";
import TrackerRecordEditor from "../components/TrackerRecordEditor";
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
import { readDashboardHandoff } from "../types/dashboardHandoff";
import type { TrackerEditorFocus } from "../components/TrackerRecordEditor";

export default function ApplicationsPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const handledHandoffState = useRef<unknown>(null);
  const [applications, setApplications] = useState<
    Application[]
  >([]);

  const [jobs, setJobs] = useState<JobOpportunity[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
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

  const [editingRecord, setEditingRecord] =
    useState<ApplicationTrackerRow | null>(null);
  const [creatingForJobId, setCreatingForJobId] =
    useState<number | null>(null);
  const [editorFocus, setEditorFocus] =
    useState<TrackerEditorFocus | undefined>();
  const [handoffMessage, setHandoffMessage] = useState("");

  const [deletingId, setDeletingId] =
    useState<number | null>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [isTrackerLoading, setIsTrackerLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [isImportOpen, setIsImportOpen] = useState(false);
  const [exporting, setExporting] = useState<string | null>(null);
  const [exportError, setExportError] = useState("");

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

  const loadCompanies = useCallback(async () => {
    try {
      setCompanies(await getCompanies());
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Companies could not be loaded.",
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
        const [applicationResults, jobResults, companyResults] =
          await Promise.all([
            getApplications(),
            getJobs(),
            getCompanies(),
          ]);

        if (!isCancelled) {
          setApplications(applicationResults);
          setJobs(jobResults);
          setCompanies(companyResults);
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
    const handoff = readDashboardHandoff(location.state);
    if (
      !handoff ||
      handledHandoffState.current === location.state
    ) {
      return;
    }
    const request = handoff;
    handledHandoffState.current = location.state;
    let isCancelled = false;

    async function resolveHandoff() {
      setHandoffMessage("");

      try {
        const row = await getApplicationTrackerRow(
          request.jobOpportunityId,
        );
        if (isCancelled) {
          return;
        }

        if (
          request.applicationId !== undefined &&
          row.applicationId !== request.applicationId
        ) {
          setHandoffMessage(
            "This Dashboard action references an application that no longer exists. The normal tracker is still available below.",
          );
          return;
        }

        setIsImportOpen(false);
        if (request.action === "APPLY" && row.applicationId === null) {
          setEditingRecord(null);
          setEditorFocus(undefined);
          setCreatingForJobId(row.jobOpportunityId);
          return;
        }

        if (row.applicationId === null) {
          setHandoffMessage(
            "This Dashboard action requires an application record, but that application no longer exists. The normal tracker is still available below.",
          );
          return;
        }

        const focus: TrackerEditorFocus = request.action === "FOLLOW_UP"
          ? "FOLLOW_UP"
          : request.action === "PREPARE_INTERVIEW"
            ? "INTERVIEW_PREPARATION"
            : "APPLICATION_DETAILS";
        setCreatingForJobId(null);
        setEditingRecord(row);
        setEditorFocus(focus);
      } catch {
        if (!isCancelled) {
          setHandoffMessage(
            "This Dashboard action could not be opened because the job no longer exists. The normal tracker is still available below.",
          );
        }
      } finally {
        if (!isCancelled) {
          navigate(location.pathname, {
            replace: true,
            state: null,
          });
        }
      }
    }

    void resolveHandoff();
    return () => {
      isCancelled = true;
    };
  }, [location.pathname, location.state, navigate]);

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

  async function handleApplicationUpdate(
    input: ApplicationInput,
  ): Promise<Application> {
    if (editingRecord?.applicationId == null) {
      throw new Error(
        "No application has been selected for editing.",
      );
    }

    const application = await updateApplication(
      editingRecord.applicationId,
      input,
    );

    await Promise.all([
      loadApplications(),
      loadTracker(trackerQuery),
    ]);

    return application;
  }

  async function handleJobUpdate(
    input: JobOpportunityInput,
  ): Promise<JobOpportunity> {
    if (!editingRecord) {
      throw new Error("No tracker record has been selected for editing.");
    }

    const job = await updateJob(editingRecord.jobOpportunityId, input);
    await Promise.all([
      loadJobs(),
      loadCompanies(),
      loadTracker(trackerQuery),
    ]);
    return job;
  }

  async function handleDelete(row: ApplicationTrackerRow) {
    if (row.applicationId === null) {
      setError("The selected tracker row has no application to delete.");
      return;
    }

    const confirmed = window.confirm(
      `Delete the application for ${row.positionTitle}? This action cannot be undone.`,
    );

    if (!confirmed) {
      return;
    }

    setDeletingId(row.applicationId);
    setError("");
    setMessage("");

    try {
      await deleteApplication(row.applicationId);

      if (
        editingRecord?.applicationId === row.applicationId
      ) {
        setEditingRecord(null);
      }

      setMessage(
        `${row.positionTitle} application was deleted.`,
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

  function handleEdit(row: ApplicationTrackerRow) {
    setEditingRecord(row);
    setCreatingForJobId(null);
    setIsImportOpen(false);
    setMessage("");
    setError("");
    setEditorFocus(undefined);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  function handleAddApplication(jobOpportunityId: number) {
    setEditingRecord(null);
    setCreatingForJobId(jobOpportunityId);
    setMessage("");
    setError("");
    setEditorFocus(undefined);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  async function handleExport(
    mode: "CURRENT_VIEW" | "ALL",
    format: "csv" | "xlsx",
  ) {
    if (exporting) {
      return;
    }

    const operation = `${mode}-${format}`;
    setExporting(operation);
    setExportError("");
    try {
      const download = await (format === "xlsx"
        ? exportApplicationTrackerXlsx
        : exportApplicationTrackerCsv)(
        mode,
        mode === "CURRENT_VIEW" ? trackerQuery : {},
      );
      const url = URL.createObjectURL(download.blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = download.filename ?? `careeros-applications.${format}`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (caughtError) {
      setExportError(
        caughtError instanceof Error
          ? caughtError.message
          : "Applications could not be exported.",
      );
    } finally {
      setExporting(null);
    }
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
        <button
          type="button"
          onClick={() => {
            setEditingRecord(null);
            setCreatingForJobId(null);
            setIsImportOpen(true);
          }}
        >
          Import Jobs
        </button>
        <div aria-label="Export applications">
          <span>Export</span>{" "}
          <button
            type="button"
            disabled={exporting !== null}
            onClick={() => handleExport("CURRENT_VIEW", "csv")}
          >
            {exporting === "CURRENT_VIEW-csv"
              ? "Exporting…"
              : "Current View (.csv)"}
          </button>{" "}
          <button
            type="button"
            disabled={exporting !== null}
            onClick={() => handleExport("ALL", "csv")}
          >
            {exporting === "ALL-csv"
              ? "Exporting…"
              : "All Applications (.csv)"}
          </button>
          {" "}
          <button
            type="button"
            disabled={exporting !== null}
            onClick={() => handleExport("CURRENT_VIEW", "xlsx")}
          >
            {exporting === "CURRENT_VIEW-xlsx"
              ? "Exporting…"
              : "Current View (.xlsx)"}
          </button>{" "}
          <button
            type="button"
            disabled={exporting !== null}
            onClick={() => handleExport("ALL", "xlsx")}
          >
            {exporting === "ALL-xlsx"
              ? "Exporting…"
              : "All Applications (.xlsx)"}
          </button>
        </div>
        {exportError && <p role="alert">{exportError}</p>}
      </header>

      {isImportOpen && (
        <ImportJobsPanel
          onClose={() => setIsImportOpen(false)}
          onImportComplete={() => Promise.all([
            loadTracker(trackerQuery),
            loadApplications(),
            loadJobs(),
            loadCompanies(),
          ]).then(() => undefined)}
        />
      )}

      {editingRecord ? (
        <TrackerRecordEditor
          key={`tracker-record-${editingRecord.jobOpportunityId}-${editingRecord.applicationId ?? "job-only"}`}
          row={editingRecord}
          companies={companies}
          jobs={jobs}
          onSaveJob={handleJobUpdate}
          onSaveApplication={handleApplicationUpdate}
          onAddApplication={handleAddApplication}
          onClose={() => {
            setEditingRecord(null);
            setEditorFocus(undefined);
          }}
          initialFocus={editorFocus}
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
          initialFocusField={
            creatingForJobId === null ? undefined : "jobOpportunityId"
          }
        />
      )}

      {handoffMessage && <p role="alert">{handoffMessage}</p>}

      {message && (
        <p
          className="status-message"
          role="status"
        >
          {message}
        </p>
      )}

      {!editingRecord &&
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
