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
  getApplications,
  updateApplication,
} from "../api/applicationsApi";
import ApplicationForm from "../components/ApplicationForm";
import ApplicationList from "../components/ApplicationList";
import type {
  Application,
  ApplicationInput,
} from "../types/application";
import { emptyApplicationInput } from "../types/applicationFormDefaults";

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

  const [editingApplication, setEditingApplication] =
    useState<Application | null>(null);

  const [deletingId, setDeletingId] =
    useState<number | null>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

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

  async function handleCreate(
    input: ApplicationInput,
  ): Promise<Application> {
    const application =
      await createApplication(input);

    setMessage(
      `${application.positionTitle} application was added.`,
    );

    await Promise.all([
      loadApplications(),
      loadJobs(),
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

    await loadApplications();

    return application;
  }

  async function handleDelete(
    application: Application,
  ) {
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

  function handleEdit(application: Application) {
    setEditingApplication(application);
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
      </header>

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
          key="create-application"
          heading="Add application"
          submitLabel="Add application"
          jobs={availableJobs}
          initialValues={emptyApplicationInput}
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

      {isLoading && <p>Loading applications...</p>}

      {error && <p role="alert">{error}</p>}

      {!isLoading && !error && (
        <ApplicationList
          applications={applications}
          deletingId={deletingId}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      )}
    </main>
  );
}