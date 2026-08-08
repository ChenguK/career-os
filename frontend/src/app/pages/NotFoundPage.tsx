import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <main className="not-found-page">
      <h1>Page not found</h1>

      <p>
        The Career OS page you requested does not exist.
      </p>

      <Link to="/dashboard">
        Return to Dashboard
    </Link>
    </main>
  );
}