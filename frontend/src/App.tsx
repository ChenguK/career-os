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
import ApplicantProfilePage from "./features/profile/pages/ApplicantProfilePage";
import ApprovedAnswersPage from "./features/answers/pages/ApprovedAnswersPage";
import QuestionsPage from "./features/questions/QuestionsPage";

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
          path="profile"
          element={<ApplicantProfilePage />}
        />

        <Route
          path="approved-answers"
          element={<ApprovedAnswersPage />}
        />
        <Route path="questions" element={<QuestionsPage />} />

        <Route
          path="*"
          element={<NotFoundPage />}
        />
      </Route>
    </Routes>
  );
}
