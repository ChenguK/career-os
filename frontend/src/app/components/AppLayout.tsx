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
      </nav>

      <Outlet />
    </>
  );
}