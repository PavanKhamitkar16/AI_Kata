import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Navigation sidebar with role-based menu items.
 *
 * Role visibility matrix:
 *   ADMIN  — all items
 *   DOCTOR — Patients, Appointments, Medical Records
 *   STAFF  — Patients, Appointments
 *
 * Future stories will add real routes; placeholder items are shown
 * with `to="#"` until their pages are implemented.
 */
const NAV_ITEMS = [
  { label: 'Dashboard',       to: '/dashboard',  roles: ['ADMIN', 'DOCTOR', 'STAFF'] },
  { label: 'Patients',        to: '#',           roles: ['ADMIN', 'DOCTOR', 'STAFF'] },
  { label: 'Appointments',    to: '#',           roles: ['ADMIN', 'DOCTOR', 'STAFF'] },
  { label: 'Medical Records', to: '#',           roles: ['ADMIN', 'DOCTOR'] },
  { label: 'Users',           to: '#',           roles: ['ADMIN'] },
  { label: 'Reports',         to: '#',           roles: ['ADMIN'] },
];

function Sidebar() {
  const { user } = useAuth();
  const role = user?.role;

  const visibleItems = NAV_ITEMS.filter(
    item => !role || item.roles.includes(role)
  );

  return (
    <aside style={styles.sidebar} data-testid="app-sidebar">
      <nav>
        <ul style={styles.navList}>
          {visibleItems.map(item => (
            <li key={item.label}>
              <NavLink
                to={item.to}
                style={({ isActive }) => ({
                  ...styles.navLink,
                  ...(isActive && item.to !== '#' ? styles.navLinkActive : {}),
                })}
              >
                {item.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}

const styles = {
  sidebar: {
    width: '220px',
    background: '#1a202c',
    color: '#cbd5e0',
    flexShrink: 0,
    paddingTop: '16px',
  },
  navList:       { listStyle: 'none', margin: 0, padding: 0 },
  navLink: {
    display: 'block',
    padding: '10px 20px',
    color: '#a0aec0',
    textDecoration: 'none',
    fontSize: '14px',
    borderLeft: '3px solid transparent',
    transition: 'background 0.15s, color 0.15s',
  },
  navLinkActive: {
    color: '#fff',
    background: '#2d3748',
    borderLeftColor: '#63b3ed',
  },
};

export default Sidebar;
