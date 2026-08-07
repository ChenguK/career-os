import type { JobOpportunity } from "../types/job";

interface JobListProps {
  jobs: JobOpportunity[];
  deletingId: number | null;
  onEdit: (job: JobOpportunity) => void;
  onDelete: (job: JobOpportunity) => Promise<void>;
}

function formatSalary(job: JobOpportunity): string | null {
  if (job.salaryMin === null && job.salaryMax === null) {
    return null;
  }

  const formatter = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: job.salaryCurrency,
    maximumFractionDigits: 0,
  });

  if (job.salaryMin !== null && job.salaryMax !== null) {
    return `${formatter.format(job.salaryMin)}–${formatter.format(
      job.salaryMax,
    )}`;
  }

  if (job.salaryMin !== null) {
    return `From ${formatter.format(job.salaryMin)}`;
  }

  return `Up to ${formatter.format(job.salaryMax ?? 0)}`;
}

export default function JobList({ 
    jobs, 
    deletingId, 
    onEdit, 
    onDelete 
}: JobListProps) {
  if (jobs.length === 0) {
    return <p>No jobs found.</p>;
  }

  return (
    <section aria-labelledby="job-list-heading">
      <h2 id="job-list-heading">Jobs</h2>

      <div className="job-list">
        {jobs.map((job) => {
          const salary = formatSalary(job);

          return (
            <article className="job-card" key={job.id}>
              <div className="job-card__heading">
                <div>
                  <h3>{job.positionTitle}</h3>

                  {job.companyName && <p>{job.companyName}</p>}
                </div>

                <span className="job-card__priority">
                  Priority {job.priority}
                </span>
              </div>

              <div className="job-card__details">
                <p>
                  <strong>Work arrangement:</strong>{" "}
                  {job.remoteType}
                </p>

                {job.location && (
                  <p>
                    <strong>Location:</strong> {job.location}
                  </p>
                )}

                {job.employmentType && (
                  <p>
                    <strong>Employment:</strong>{" "}
                    {job.employmentType}
                  </p>
                )}

                {salary && (
                  <p>
                    <strong>Salary:</strong> {salary}
                  </p>
                )}

                {job.matchScore !== null && (
                  <p>
                    <strong>Match:</strong> {job.matchScore}/10
                  </p>
                )}
              </div>

              {job.applicationUrl && (
                <a
                  href={job.applicationUrl}
                  target="_blank"
                  rel="noreferrer"
                >
                  View application
                </a>
                
              )}
              <div className="job-card__actions">
                <button
                    type="button"
                    onClick={() => onEdit(job)}
                >
                    Edit
                </button>

                <button
                    type="button"
                    disabled={deletingId === job.id}
                    onClick={() => void onDelete(job)}
                >
                    {deletingId === job.id
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