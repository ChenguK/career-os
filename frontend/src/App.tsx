import {
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import AppLayout from "./app/components/AppLayout";
import NotFoundPage from "./app/pages/NotFoundPage";
import DashboardPage from "./features/dashboard/pages/DashboardPage";
import ApplicationsPage from "./features/applications/pages/ApplicationsPage";
import CompaniesPage from "./features/companies/pages/CompaniesPage";
import JobsPage from "./features/jobs/pages/JobsPage";

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route
          index
          element={
            <Navigate
              to="/dashboard"
              replace
            />
          }
        />

        <Route
          path="dashboard"
          element={<DashboardPage />}
        />

        <Route
          path="applications"
          element={<ApplicationsPage />}
        />

        <Route
          path="jobs"
          element={<JobsPage />}
        />

        <Route
          path="companies"
          element={<CompaniesPage />}
        />

        <Route
          path="*"
          element={<NotFoundPage />}
        />
      </Route>
    </Routes>
  );
}