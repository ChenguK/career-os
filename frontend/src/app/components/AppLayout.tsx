import {
  NavLink,
  Outlet,
} from "react-router-dom";

export default function AppLayout() {
  return (
    <>
      <nav
        className="app-navigation"
        aria-label="Main navigation"
      >
        <NavLink
            to="/dashboard"
            className={({ isActive }) =>
                isActive ? "active" : undefined
            }
            >
            Dashboard
        </NavLink>

        <NavLink
          to="/applications"
          className={({ isActive }) =>
            isActive ? "active" : undefined
          }
        >
          Applications
        </NavLink>

        <NavLink
          to="/jobs"
          className={({ isActive }) =>
            isActive ? "active" : undefined
          }
        >
          Jobs
        </NavLink>

        <NavLink
          to="/companies"
          className={({ isActive }) =>
            isActive ? "active" : undefined
          }
        >
          Companies
        </NavLink>

        <NavLink
          to="/profile"
          className={({ isActive }) =>
            isActive ? "active" : undefined
          }
        >
          Applicant Profile
        </NavLink>

        <NavLink
          to="/approved-answers"
          className={({ isActive }) =>
            isActive ? "active" : undefined
          }
        >
          Approved Answers
        </NavLink>
        <NavLink to="/questions" className={({ isActive }) => isActive ? "active" : undefined}>Questions</NavLink>
      </nav>

      <Outlet />
    </>
  );
}
