import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import { notificationApi } from '../../api/dashboardApi.js';
import { formatDateTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function NotificationsPage() {
  const { showToast } = useToast();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [unreadOnly, setUnreadOnly] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const { data } = await notificationApi.getMine(unreadOnly);
      setNotifications(data.data || []);
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [unreadOnly]);

  async function markRead(id) {
    try {
      await notificationApi.markAsRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  async function markAllRead() {
    try {
      await notificationApi.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      showToast('All notifications marked as read');
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  return (
    <DashboardLayout>
      <PageHeader
        title="Notifications"
        subtitle="System updates related to your account and activity."
        action={
          <>
            <button
              className={unreadOnly ? 'btn-primary' : 'btn-secondary'}
              onClick={() => setUnreadOnly((v) => !v)}
            >
              {unreadOnly ? 'Showing unread' : 'Show unread only'}
            </button>
            <button className="btn-secondary" onClick={markAllRead}>
              Mark all read
            </button>
          </>
        }
      />

      {loading ? (
        <Loader />
      ) : notifications.length === 0 ? (
        <EmptyState title="No notifications" subtitle="You're all caught up." icon="🔔" />
      ) : (
        <div className="space-y-3">
          {notifications.map((n) => (
            <div
              key={n.id}
              className={`card flex items-start justify-between gap-4 p-4 ${
                !n.read ? 'border-l-4 border-l-brand-500' : ''
              }`}
            >
              <div>
                <p className="font-semibold text-ink-900">{n.title}</p>
                <p className="mt-1 text-sm text-ink-600">{n.message}</p>
                <p className="mt-2 text-xs text-ink-400">{formatDateTime(n.createdAt)}</p>
              </div>
              {!n.read && (
                <button onClick={() => markRead(n.id)} className="btn-ghost shrink-0 text-xs">
                  Mark read
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </DashboardLayout>
  );
}
