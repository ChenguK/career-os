import type {
  ApplicationStatus,
} from "../types/application";
import type { ApplicationTrackerRow } from "../types/applicationTracker";
import type { RemoteType } from "../../jobs/types/job";

interface ApplicationListProps {
  rows: ApplicationTrackerRow[];
  deletingId: number | null;
  onEdit: (applicationId: number) => void;
  onDelete: (applicationId: number) => Promise<void>;
  onAddApplication: (jobOpportunityId: number) => void;
}

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

const remoteTypeLabels: Record<RemoteType, string> = {
  REMOTE: "Remote",
  HYBRID: "Hybrid",
  ONSITE: "Onsite",
  UNKNOWN: "Unknown",
};

function formatDate(value: string | null): string {
  if (!value) {
    return "—";
  }

  const [year, month, day] = value.split("-").map(Number);

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(new Date(year, month - 1, day));
}

function formatSalary(row: ApplicationTrackerRow): string {
  if (row.salaryMin === null && row.salaryMax === null) {
    return "—";
  }

  const formatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: row.salaryCurrency,
    maximumFractionDigits: 0,
  });

  if (row.salaryMin !== null && row.salaryMax !== null) {
    return `${formatter.format(row.salaryMin)}–${formatter.format(
      row.salaryMax,
    )}`;
  }

  if (row.salaryMin !== null) {
    return `From ${formatter.format(row.salaryMin)}`;
  }

  return `Up to ${formatter.format(row.salaryMax ?? 0)}`;
}

export default function ApplicationList({
  rows,
  deletingId,
  onEdit,
  onDelete,
  onAddApplication,
}: ApplicationListProps) {
  if (rows.length === 0) {
    return <p>No tracked jobs found.</p>;
  }

  return (
    <section aria-labelledby="application-list-heading">
      <h2 id="application-list-heading">Applications</h2>

      <div className="application-table-container">
        <table className="application-table">
          <thead>
            <tr>
              <th scope="col">Company</th>
              <th scope="col">Position Title</th>
              <th scope="col">Status</th>
              <th scope="col">Priority</th>
              <th scope="col">Match Score</th>
              <th scope="col">Location</th>
              <th scope="col">Work Arrangement</th>
              <th scope="col">Employment Type</th>
              <th scope="col">Salary</th>
              <th scope="col">Application URL</th>
              <th scope="col">Date Posted</th>
              <th scope="col">Application Date</th>
              <th scope="col">Follow-Up Date</th>
              <th scope="col">Resume Version</th>
              <th scope="col">Cover Letter Needed</th>
              <th scope="col">Source</th>
              <th scope="col">Actions</th>
            </tr>
          </thead>

          <tbody>
            {rows.map((row) => {
              const hasApplication = row.applicationId !== null;

              return (
                <tr key={row.jobOpportunityId}>
                  <td>{row.companyName ?? "—"}</td>
                  <th scope="row">{row.positionTitle}</th>
                  <td>
                    {row.status
                      ? statusLabels[row.status]
                      : statusLabels.SAVED}
                  </td>
                  <td>{row.priority}</td>
                  <td>
                    {row.matchScore === null
                      ? "—"
                      : `${row.matchScore}/10`}
                  </td>
                  <td>{row.location ?? "—"}</td>
                  <td>{remoteTypeLabels[row.remoteType]}</td>
                  <td>{row.employmentType ?? "—"}</td>
                  <td>{formatSalary(row)}</td>
                  <td>
                    {row.applicationUrl ? (
                      <a
                        href={row.applicationUrl}
                        target="_blank"
                        rel="noreferrer"
                      >
                        Open job
                      </a>
                    ) : (
                      "—"
                    )}
                  </td>
                  <td>{formatDate(row.datePosted)}</td>
                  <td>{formatDate(row.applicationDate)}</td>
                  <td>{formatDate(row.followUpDate)}</td>
                  <td>{row.resumeVersion ?? "—"}</td>
                  <td>
                    {row.coverLetterNeeded === null
                      ? "—"
                      : row.coverLetterNeeded
                        ? "Yes"
                        : "No"}
                  </td>
                  <td>{row.source ?? "—"}</td>
                  <td>
                    <div className="application-table__actions">
                      {hasApplication ? (
                        <>
                          <button
                            type="button"
                            aria-label={`Edit application for ${row.positionTitle}`}
                            onClick={() =>
                              onEdit(row.applicationId!)
                            }
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            aria-label={`Delete application for ${row.positionTitle}`}
                            disabled={
                              deletingId === row.applicationId
                            }
                            onClick={() =>
                              void onDelete(row.applicationId!)
                            }
                          >
                            {deletingId === row.applicationId
                              ? "Deleting..."
                              : "Delete"}
                          </button>
                        </>
                      ) : (
                        <button
                          type="button"
                          aria-label={`Add application for ${row.positionTitle}`}
                          onClick={() =>
                            onAddApplication(row.jobOpportunityId)
                          }
                        >
                          Add application
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}
