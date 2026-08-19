import {
  NavLink,
  Outlet,
  useLocation,
} from "react-router-dom";
import { useState } from "react";

export default function AppLayout() {
  const location = useLocation();
  const [profileMenuOpen, setProfileMenuOpen] = useState(false);

  function closeProfileMenu() {
    setProfileMenuOpen(false);
  }

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

        <div
          className="app-navigation__dropdown"
          onMouseEnter={() => setProfileMenuOpen(true)}
          onMouseLeave={() => setProfileMenuOpen(false)}
          onFocus={() => setProfileMenuOpen(true)}
          onBlur={(event) => {
            if (!event.currentTarget.contains(event.relatedTarget)) closeProfileMenu();
          }}
          onKeyDown={(event) => {
            if (event.key === "Escape") {
              closeProfileMenu();
              event.currentTarget.querySelector<HTMLButtonElement>("button")?.focus();
            }
          }}
        >
          <button
            type="button"
            className={location.pathname === "/profile" || location.pathname === "/materials" ? "active" : undefined}
            aria-haspopup="menu"
            aria-expanded={profileMenuOpen}
            onClick={() => setProfileMenuOpen(true)}
          >
            Applicant Profile
          </button>
          {profileMenuOpen && (
            <div className="app-navigation__menu" role="menu">
              <NavLink to="/profile" role="menuitem" onClick={closeProfileMenu}>Profile</NavLink>
              <NavLink to="/materials" role="menuitem" onClick={closeProfileMenu}>Materials</NavLink>
            </div>
          )}
        </div>

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
