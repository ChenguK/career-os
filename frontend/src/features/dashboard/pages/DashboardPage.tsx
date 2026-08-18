import {
  useEffect,
  useMemo,
  useState,
} from "react";
import { Link } from "react-router-dom";

import { getApplications } from "../../applications/api/applicationsApi";
import type {
  Application,
  ApplicationStatus,
} from "../../applications/types/application";
import { getCompanies } from "../../companies/api/companiesApi";
import type { Company } from "../../companies/types/company";
import { getJobs } from "../../jobs/api/jobsApi";
import type { JobOpportunity } from "../../jobs/types/job";
import { dashboardHandoffState } from "../../applications/types/dashboardHandoff";

interface UpcomingInterview {
  applicationId: number;
  jobOpportunityId: number;
  companyName: string | null;
  positionTitle: string;
  type: string;
  dateTime: string;
}

const terminalStatuses: ApplicationStatus[] = [
  "OFFER",
  "REJECTED",
  "WITHDRAWN",
  "CLOSED",
];

const activeInterviewStatuses: ApplicationStatus[] = [
  "PHONE_SCREEN",
  "INTERVIEW_ONE",
  "INTERVIEW_TWO",
];

const statusLabels: Record<ApplicationStatus, string> = {
  SAVED: "Saved",
  PREPARING: "Preparing",
  APPLIED: "Applied",
  PHONE_SCREEN: "Phone Screen",
  INTERVIEW_ONE: "Interview 1",
  INTERVIEW_TWO: "Interview 2",
  OFFER: "Offer",
  REJECTED: "Rejected",
  WITHDRAWN: "Withdrawn",
  CLOSED: "Closed",
};

function getLocalDateString(): string {
  const today = new Date();

  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function formatDate(value: string): string {
  const [year, month, day] = value.split("-").map(Number);

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(
    new Date(year, month - 1, day),
  );
}

function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

export default function DashboardPage() {
  const [applications, setApplications] =
    useState<Application[]>([]);

  const [jobs, setJobs] =
    useState<JobOpportunity[]>([]);

  const [companies, setCompanies] =
    useState<Company[]>([]);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isCancelled = false;

    async function loadDashboard() {
      setIsLoading(true);
      setError("");

      try {
        const [
          applicationResults,
          jobResults,
          companyResults,
        ] = await Promise.all([
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
              : "Dashboard data could not be loaded.",
          );
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadDashboard();

    return () => {
      isCancelled = true;
    };
  }, []);

  const today = getLocalDateString();

  const activeInterviews = useMemo(
    () =>
      applications.filter((application) =>
        activeInterviewStatuses.includes(
          application.status,
        ),
      ).length,
    [applications],
  );

  const offers = useMemo(
    () =>
      applications.filter(
        (application) =>
          application.status === "OFFER",
      ).length,
    [applications],
  );

  const highPriorityJobs = useMemo(
    () =>
      jobs.filter(
        (job) =>
          job.priority === 1 ||
          job.priority === 2,
      ).length,
    [jobs],
  );

  const unappliedHighPriorityJobs = useMemo(() => {
  const appliedJobIds = new Set(
    applications.map(
      (application) => application.jobOpportunityId,
    ),
  );

  return jobs.filter(
    (job) =>
      (job.priority === 1 || job.priority === 2) &&
      !appliedJobIds.has(job.id),
    );
    }, [applications, jobs]);

  const dreamCompanies = useMemo(
    () =>
      companies.filter(
        (company) => company.dreamCompany,
      ).length,
    [companies],
  );

  const preparingApplications = useMemo(
  () =>
    applications.filter(
      (application) =>
        application.status === "PREPARING",
    ),
    [applications],
    );

  const followUpsDue = useMemo(
    () =>
      applications
        .filter((application) => {
          if (!application.followUpDate) {
            return false;
          }

          if (
            terminalStatuses.includes(
              application.status,
            )
          ) {
            return false;
          }

          return application.followUpDate <= today;
        })
        .sort((first, second) =>
          (first.followUpDate ?? "").localeCompare(
            second.followUpDate ?? "",
          ),
        ),
    [applications, today],
  );

  const upcomingInterviews = useMemo(() => {
    const now = new Date();

    const results: UpcomingInterview[] = [];

    applications.forEach((application) => {
      const events = [
        {
          type: "Phone Screen",
          value: application.phoneScreenAt,
        },
        {
          type: "Interview 1",
          value: application.interviewOneAt,
        },
        {
          type: "Interview 2",
          value: application.interviewTwoAt,
        },
      ];

      events.forEach((event) => {
        if (!event.value) {
          return;
        }

        const eventDate = new Date(event.value);

        if (eventDate < now) {
          return;
        }

        results.push({
          applicationId: application.id,
          jobOpportunityId: application.jobOpportunityId,
          companyName: application.companyName,
          positionTitle: application.positionTitle,
          type: event.type,
          dateTime: event.value,
        });
      });
    });

    return results.sort(
      (first, second) =>
        new Date(first.dateTime).getTime() -
        new Date(second.dateTime).getTime(),
    );
  }, [applications]);

  const actionCount =
        followUpsDue.length +
        preparingApplications.length +
        upcomingInterviews.length +
        unappliedHighPriorityJobs.length;


  const statusCounts = useMemo(() => {
    const counts = Object.fromEntries(
      Object.keys(statusLabels).map(
        (status) => [status, 0],
      ),
    ) as Record<ApplicationStatus, number>;

    applications.forEach((application) => {
      counts[application.status] += 1;
    });

    return counts;
  }, [applications]);

  if (isLoading) {
    return (
      <main className="dashboard-page">
        <p>Loading dashboard...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="dashboard-page">
        <p role="alert">{error}</p>
      </main>
    );
  }

  return (
    <main className="dashboard-page">
      <header>
        <p>Career OS</p>
        <h1>Dashboard</h1>
        <p>
          Your job search at a glance and the next
          actions that need your attention.
        </p>
      </header>

      <section
        className="dashboard-stats"
        aria-label="Career summary"
      >
        <article className="dashboard-stat">
          <span>Applications</span>
          <strong>{applications.length}</strong>
        </article>

        <article className="dashboard-stat">
          <span>Active interviews</span>
          <strong>{activeInterviews}</strong>
        </article>

        <article className="dashboard-stat">
          <span>Offers</span>
          <strong>{offers}</strong>
        </article>

        <article className="dashboard-stat">
          <span>High-priority jobs</span>
          <strong>{highPriorityJobs}</strong>
        </article>

        <article className="dashboard-stat">
          <span>Dream companies</span>
          <strong>{dreamCompanies}</strong>
        </article>

        <article className="dashboard-stat">
          <span>Follow-ups due</span>
          <strong>{followUpsDue.length}</strong>
        </article>
      </section>

      <section aria-labelledby="todays-actions-heading">
  <h2 id="todays-actions-heading">
    Today's Actions
  </h2>

  <p>
    {actionCount === 0
      ? "You're caught up."
      : `${actionCount} action${
          actionCount === 1 ? "" : "s"
        } need attention.`}
  </p>

  <div className="dashboard-list">
    {followUpsDue.map((application) => (
      <article
        className="dashboard-item"
        key={`follow-up-${application.id}`}
      >
        <h3>Follow up</h3>

        <p>{application.positionTitle}</p>

        {application.companyName && (
          <p>{application.companyName}</p>
        )}

        <p>
          Due{" "}
          {formatDate(
            application.followUpDate!,
          )}
        </p>
        <Link
          to="/applications"
          state={dashboardHandoffState({
            action: "FOLLOW_UP",
            jobOpportunityId: application.jobOpportunityId,
            applicationId: application.id,
          })}
        >
          Open follow-up
        </Link>
      </article>
    ))}

    {preparingApplications.map((application) => (
      <article
        className="dashboard-item"
        key={`preparing-${application.id}`}
      >
        <h3>Finish application</h3>

        <p>{application.positionTitle}</p>

        {application.companyName && (
          <p>{application.companyName}</p>
        )}
        <Link
          to="/applications"
          state={dashboardHandoffState({
            action: "FINISH_APPLICATION",
            jobOpportunityId: application.jobOpportunityId,
            applicationId: application.id,
          })}
        >
          Finish application
        </Link>
      </article>
    ))}

    {upcomingInterviews.map((interview) => (
      <article
        className="dashboard-item"
        key={`interview-${interview.applicationId}-${interview.type}`}
      >
        <h3>Prepare for {interview.type}</h3>

        <p>{interview.positionTitle}</p>

        {interview.companyName && (
          <p>{interview.companyName}</p>
        )}

        <p>
          {formatDateTime(interview.dateTime)}
        </p>
        <Link
          to="/applications"
          state={dashboardHandoffState({
            action: "PREPARE_INTERVIEW",
            jobOpportunityId: interview.jobOpportunityId,
            applicationId: interview.applicationId,
          })}
        >
          Prepare for interview
        </Link>
      </article>
    ))}

    {unappliedHighPriorityJobs.map((job) => (
      <article
        className="dashboard-item"
        key={`job-${job.id}`}
      >
        <h3>Apply to priority job</h3>

        <p>{job.positionTitle}</p>

        {job.companyName && (
          <p>{job.companyName}</p>
        )}

        <p>Priority {job.priority}</p>
        <Link
          to="/applications"
          state={dashboardHandoffState({
            action: "APPLY",
            jobOpportunityId: job.id,
          })}
        >
          Start application
        </Link>
      </article>
        ))}
    </div>
    </section>

      <section aria-labelledby="needs-attention-heading">
        <h2 id="needs-attention-heading">
          Needs Attention
        </h2>

        {followUpsDue.length === 0 ? (
          <p>No application follow-ups are due.</p>
        ) : (
          <div className="dashboard-list">
            {followUpsDue.map((application) => (
              <article
                className="dashboard-item"
                key={application.id}
              >
                <h3>{application.positionTitle}</h3>

                {application.companyName && (
                  <p>{application.companyName}</p>
                )}

                <p>
                  <strong>Follow up:</strong>{" "}
                  {formatDate(
                    application.followUpDate!,
                  )}
                </p>

                <p>
                  <strong>Status:</strong>{" "}
                  {statusLabels[application.status]}
                </p>
              </article>
            ))}
          </div>
        )}
      </section>

      <section
        aria-labelledby="upcoming-interviews-heading"
      >
        <h2 id="upcoming-interviews-heading">
          Upcoming Interviews
        </h2>

        {upcomingInterviews.length === 0 ? (
          <p>No upcoming interviews are scheduled.</p>
        ) : (
          <div className="dashboard-list">
            {upcomingInterviews.map((interview) => (
              <article
                className="dashboard-item"
                key={`${interview.applicationId}-${interview.type}`}
              >
                <h3>{interview.positionTitle}</h3>

                {interview.companyName && (
                  <p>{interview.companyName}</p>
                )}

                <p>
                  <strong>{interview.type}:</strong>{" "}
                  {formatDateTime(interview.dateTime)}
                </p>
              </article>
            ))}
          </div>
        )}
      </section>

      <section aria-labelledby="pipeline-heading">
        <h2 id="pipeline-heading">
          Hiring Pipeline
        </h2>

        <div className="pipeline-grid">
          {(
            Object.entries(statusLabels) as [
              ApplicationStatus,
              string,
            ][]
          ).map(([status, label]) => (
            <article
              className="pipeline-item"
              key={status}
            >
              <span>{label}</span>
              <strong>{statusCounts[status]}</strong>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
