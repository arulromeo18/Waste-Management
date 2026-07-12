import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.js';
import { notificationApi } from '../../api/dashboardApi.js';

export default function Navbar({ onMenuClick }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    let mounted = true;
    async function fetchUnread() {
      try {
        const { data } = await notificationApi.getUnreadCount();
        if (mounted) setUnreadCount(data.data || 0);
      } catch {
        // silently ignore — badge just stays at last known value
      }
    }
    fetchUnread();
    const interval = setInterval(fetchUnread, 30000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  function handleLogout() {
    logout();
    navigate('/login');
  }

  const notificationsPath =
    user?.role === 'CITIZEN' ? '/citizen/notifications' : '/notifications';

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-ink-200 bg-white px-4 sm:px-6">
      <div className="flex items-center gap-3">
        <button
          onClick={onMenuClick}
          className="rounded-md p-2 text-ink-500 hover:bg-ink-100 lg:hidden"
          aria-label="Toggle menu"
        >
          ☰
        </button>
        <div className="flex items-center gap-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
            🌿
          </span>
          <span className="text-lg font-bold text-ink-900">CleanCity</span>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate(notificationsPath)}
          className="relative rounded-full p-2 text-ink-500 hover:bg-ink-100"
          aria-label="Notifications"
        >
          🔔
          {unreadCount > 0 && (
            <span className="absolute -right-0.5 -top-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
              {unreadCount > 9 ? '9+' : unreadCount}
            </span>
          )}
        </button>

        <div className="relative">
          <button
            onClick={() => setMenuOpen((o) => !o)}
            className="flex items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-ink-100"
          >
            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-ink-800 text-sm font-semibold text-white">
              {user?.fullName?.charAt(0)?.toUpperCase() || '?'}
            </span>
            <span className="hidden text-sm font-medium text-ink-700 sm:block">{user?.fullName}</span>
          </button>
          {menuOpen && (
            <div className="absolute right-0 mt-2 w-48 rounded-lg border border-ink-200 bg-white py-1 shadow-lg">
              <div className="border-b border-ink-100 px-4 py-2">
                <p className="truncate text-sm font-medium text-ink-800">{user?.fullName}</p>
                <p className="truncate text-xs text-ink-400">{user?.email}</p>
              </div>
              <button
                onClick={handleLogout}
                className="block w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50"
              >
                Log out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
