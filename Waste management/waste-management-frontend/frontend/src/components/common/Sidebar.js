import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.js';

const NAV_ITEMS = {
  SUPER_ADMIN: [
    { to: '/admin/dashboard', label: 'Dashboard', icon: '📊' },
    { to: '/admin/citizens', label: 'Citizens', icon: '🧑‍🤝‍🧑' },
    { to: '/admin/workers', label: 'Workers', icon: '🧹' },
    { to: '/admin/vehicles', label: 'Vehicles', icon: '🚛' },
    { to: '/admin/zones', label: 'Zones', icon: '🗺️' },
    { to: '/admin/schedules', label: 'Schedules', icon: '📅' },
    { to: '/admin/complaints', label: 'Complaints', icon: '📮' },
    { to: '/admin/announcements', label: 'Announcements', icon: '📣' },
    { to: '/admin/rewards', label: 'Rewards', icon: '🏆' },
    { to: '/admin/penalties', label: 'Penalties', icon: '⚠️' },
    { to: '/admin/reports', label: 'Reports', icon: '🧾' },
  ],
  WORKER: [
    { to: '/worker/dashboard', label: 'Dashboard', icon: '📊' },
    { to: '/worker/today-schedule', label: "Today's Schedule", icon: '📅' },
    { to: '/worker/assigned-route', label: 'Assigned Route', icon: '🗺️' },
    { to: '/worker/upload', label: 'Log Collection', icon: '📸' },
    { to: '/notifications', label: 'Notifications', icon: '🔔' },
  ],
  CITIZEN: [
    { to: '/citizen/dashboard', label: 'Dashboard', icon: '📊' },
    { to: '/citizen/profile', label: 'My Profile', icon: '👤' },
    { to: '/citizen/schedule', label: 'Collection Schedule', icon: '📅' },
    { to: '/citizen/complaints/new', label: 'Submit Complaint', icon: '📝' },
    { to: '/citizen/complaints', label: 'My Complaints', icon: '📮' },
    { to: '/citizen/rewards', label: 'Rewards & Penalties', icon: '🏆' },
    { to: '/citizen/segregation-guide', label: 'Segregation Guide', icon: '♻️' },
    { to: '/citizen/notifications', label: 'Notifications', icon: '🔔' },
  ],
};

export default function Sidebar({ open, onClose }) {
  const { user } = useAuth();
  const items = NAV_ITEMS[user?.role] || [];

  return (
    <>
      {open && (
        <div className="fixed inset-0 z-20 bg-ink-900/30 lg:hidden" onClick={onClose} />
      )}
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 transform border-r border-ink-200 bg-white pt-16 transition-transform lg:static lg:translate-x-0 lg:pt-0 ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <nav className="flex h-full flex-col gap-1 overflow-y-auto p-3">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-brand-50 text-brand-700'
                    : 'text-ink-600 hover:bg-ink-50 hover:text-ink-900'
                }`
              }
            >
              <span className="text-base">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
