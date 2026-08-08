import type {
  Application,
  ApplicationStatus,
} from "../types/application";

interface ApplicationListProps {
  applications: Application[];
  deletingId: number | null;
  onEdit: (application: Application) => void;
  onDelete: (application: Application) => Promise<void>;
}

function formatStatus(status: ApplicationStatus): string {
  switch (status) {
    case "SAVED":
      return "Saved";

    case "PREPARING":
      return "Preparing";

    case "APPLIED":
      return "Applied";

    case "PHONE_SCREEN":
      return "Phone Screen";

    case "INTERVIEW_ONE":
      return "Interview 1";

    case "INTERVIEW_TWO":
      return "Interview 2";

    case "OFFER":
      return "Offer";

    case "REJECTED":
      return "Rejected";

    case "WITHDRAWN":
      return "Withdrawn";

    case "CLOSED":
      return "Closed";
  }
}

function formatDate(value: string | null): string | null {
  if (!value) {
    return null;
  }

  const [year, month, day] = value.split("-").map(Number);

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(
    new Date(year, month - 1, day),
  );
}

function formatDateTime(value: string | null): string | null {
  if (!value) {
    return null;
  }

  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

export default function ApplicationList({
  applications,
  deletingId,
  onEdit,
  onDelete,
}: ApplicationListProps) {
  if (applications.length === 0) {
    return <p>No applications found.</p>;
  }

  return (
    <section aria-labelledby="application-list-heading">
      <h2 id="application-list-heading">
        Applications
      </h2>

      <div className="application-list">
        {applications.map((application) => {
          const applicationDate = formatDate(
            application.applicationDate,
          );

          const followUpDate = formatDate(
            application.followUpDate,
          );

          const phoneScreen = formatDateTime(
            application.phoneScreenAt,
          );

          const interviewOne = formatDateTime(
            application.interviewOneAt,
          );

          const interviewTwo = formatDateTime(
            application.interviewTwoAt,
          );

          const offerDate = formatDateTime(
            application.offerAt,
          );

          const rejectedDate = formatDateTime(
            application.rejectedAt,
          );

          return (
            <article
              className="application-card"
              key={application.id}
            >
              <div className="application-card__heading">
                <div>
                  <h3>{application.positionTitle}</h3>

                  {application.companyName && (
                    <p>{application.companyName}</p>
                  )}
                </div>

                <span className="application-card__status">
                  {formatStatus(application.status)}
                </span>
              </div>

              <div className="application-card__details">
                {application.resumeVersion && (
                  <p>
                    <strong>Résumé:</strong>{" "}
                    {application.resumeVersion}
                  </p>
                )}

                <p>
                  <strong>Cover letter:</strong>{" "}
                  {application.coverLetterNeeded
                    ? "Needed"
                    : "Not needed"}
                </p>

                {applicationDate && (
                  <p>
                    <strong>Applied:</strong>{" "}
                    {applicationDate}
                  </p>
                )}

                {followUpDate && (
                  <p>
                    <strong>Follow up:</strong>{" "}
                    {followUpDate}
                  </p>
                )}

                {application.recruiterName && (
                  <p>
                    <strong>Recruiter:</strong>{" "}
                    {application.recruiterName}
                  </p>
                )}

                {application.recruiterEmail && (
                  <p>
                    <strong>Recruiter email:</strong>{" "}
                    <a
                      href={`mailto:${application.recruiterEmail}`}
                    >
                      {application.recruiterEmail}
                    </a>
                  </p>
                )}

                {phoneScreen && (
                  <p>
                    <strong>Phone screen:</strong>{" "}
                    {phoneScreen}
                  </p>
                )}

                {interviewOne && (
                  <p>
                    <strong>Interview 1:</strong>{" "}
                    {interviewOne}
                  </p>
                )}

                {interviewTwo && (
                  <p>
                    <strong>Interview 2:</strong>{" "}
                    {interviewTwo}
                  </p>
                )}

                {offerDate && (
                  <p>
                    <strong>Offer:</strong> {offerDate}
                  </p>
                )}

                {rejectedDate && (
                  <p>
                    <strong>Rejected:</strong>{" "}
                    {rejectedDate}
                  </p>
                )}
              </div>

              {application.projectsToHighlight && (
                <div>
                  <strong>Projects to highlight</strong>
                  <p>{application.projectsToHighlight}</p>
                </div>
              )}

              {application.skillsToEmphasize && (
                <div>
                  <strong>Skills to emphasize</strong>
                  <p>{application.skillsToEmphasize}</p>
                </div>
              )}

              {application.interviewTopics && (
                <div>
                  <strong>Interview topics</strong>
                  <p>{application.interviewTopics}</p>
                </div>
              )}

              {application.notes && (
                <div>
                  <strong>Notes</strong>
                  <p>{application.notes}</p>
                </div>
              )}

              <div className="application-card__links">
                {application.portfolioLink && (
                  <a
                    href={application.portfolioLink}
                    target="_blank"
                    rel="noreferrer"
                  >
                    Portfolio
                  </a>
                )}

                {application.githubLink && (
                  <a
                    href={application.githubLink}
                    target="_blank"
                    rel="noreferrer"
                  >
                    GitHub
                  </a>
                )}
              </div>

              <div className="application-card__actions">
                <button
                  type="button"
                  onClick={() => onEdit(application)}
                >
                  Edit
                </button>

                <button
                  type="button"
                  disabled={
                    deletingId === application.id
                  }
                  onClick={() =>
                    void onDelete(application)
                  }
                >
                  {deletingId === application.id
                    ? "Deleting..."
                    : "Delete"}
                </button>
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}