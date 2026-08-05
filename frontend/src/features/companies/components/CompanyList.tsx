import type { Company } from "../types/company";

interface CompanyListProps {
  companies: Company[];
  deletingId: number | null;
  onEdit: (company: Company) => void;
  onDelete: (company: Company) => Promise<void>;
}

export default function CompanyList({
  companies,
  deletingId,
  onEdit,
  onDelete,
}: CompanyListProps) {
  if (companies.length === 0) {
    return <p>No companies found.</p>;
  }

  return (
    <section aria-labelledby="company-list-heading">
      <h2 id="company-list-heading">Companies</h2>

      <div className="company-list">
        {companies.map((company) => (
          <article className="company-card" key={company.id}>
            <div className="company-card__heading">
              <h3>{company.name}</h3>

              {company.dreamCompany && (
                <span className="company-card__badge">
                  Dream company
                </span>
              )}
            </div>

            {company.industry && <p>{company.industry}</p>}

            {company.companyType && (
              <p>
                <strong>Type:</strong> {company.companyType}
              </p>
            )}

            {company.remotePolicy && (
              <p>
                <strong>Remote policy:</strong>{" "}
                {company.remotePolicy}
              </p>
            )}

            <div className="company-card__links">
              {company.websiteUrl && (
                <a
                  href={company.websiteUrl}
                  target="_blank"
                  rel="noreferrer"
                >
                  Website
                </a>
              )}

              {company.careersUrl && (
                <a
                  href={company.careersUrl}
                  target="_blank"
                  rel="noreferrer"
                >
                  Careers
                </a>
              )}
            </div>

            <div className="company-card__actions">
              <button
                type="button"
                onClick={() => onEdit(company)}
              >
                Edit
              </button>

              <button
                type="button"
                disabled={deletingId === company.id}
                onClick={() => void onDelete(company)}
              >
                {deletingId === company.id
                  ? "Deleting..."
                  : "Delete"}
              </button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}