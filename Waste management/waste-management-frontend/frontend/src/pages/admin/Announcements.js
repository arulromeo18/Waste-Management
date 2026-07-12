import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import Modal from '../../components/common/Modal.js';
import { announcementApi } from '../../api/announcementApi.js';
import { zoneApi } from '../../api/zoneApi.js';
import { formatDateTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

const emptyForm = { title: '', message: '', zoneId: '', priority: 'NORMAL' };

export default function Announcements() {
  const { showToast } = useToast();
  const [announcements, setAnnouncements] = useState([]);
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const [annRes, zonesRes] = await Promise.all([announcementApi.getAll(), zoneApi.getAll()]);
      setAnnouncements(annRes.data.data || []);
      setZones(zonesRes.data.data || []);
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEdit(a) {
    setEditing(a);
    setForm({ title: a.title, message: a.message, zoneId: a.zoneId || '', priority: a.priority || 'NORMAL' });
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    const payload = { ...form, zoneId: form.zoneId || null };
    try {
      if (editing) {
        await announcementApi.update(editing.id, payload);
        showToast('Announcement updated successfully');
      } else {
        await announcementApi.create(payload);
        showToast('Announcement published successfully');
      }
      setModalOpen(false);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setSaving(false);
    }
  }

  async function handleDeactivate(a) {
    if (!window.confirm(`Deactivate "${a.title}"?`)) return;
    try {
      await announcementApi.deactivate(a.id);
      showToast('Announcement deactivated');
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  function zoneName(zoneId) {
    if (!zoneId) return 'City-wide';
    return zones.find((z) => z.id === zoneId)?.zoneName || zoneId;
  }

  return (
    <DashboardLayout>
      <PageHeader
        title="Announcements"
        subtitle="Broadcast schedule changes and civic notices to citizens and workers."
        action={
          <button className="btn-primary" onClick={openCreate}>
            + New Announcement
          </button>
        }
      />

      {loading ? (
        <Loader />
      ) : announcements.length === 0 ? (
        <EmptyState title="No announcements yet" subtitle="Publish your first announcement." icon="📣" />
      ) : (
        <div className="space-y-3">
          {announcements.map((a) => (
            <div key={a.id} className="card p-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-semibold text-ink-900">{a.title}</h3>
                    <Badge status={a.active ? 'ACTIVE' : 'INACTIVE'}>{a.active ? 'Active' : 'Inactive'}</Badge>
                  </div>
                  <p className="mt-1 text-xs text-ink-400">
                    {zoneName(a.zoneId)} · {formatDateTime(a.createdAt)}
                  </p>
                  <p className="mt-2 text-sm text-ink-600">{a.message}</p>
                </div>
                <div className="flex shrink-0 gap-2">
                  <button className="btn-secondary text-xs" onClick={() => openEdit(a)}>
                    Edit
                  </button>
                  {a.active && (
                    <button className="btn-danger text-xs" onClick={() => handleDeactivate(a)}>
                      Deactivate
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? 'Edit Announcement' : 'New Announcement'}
      >
        <form id="announcement-form" onSubmit={handleSave} className="space-y-4">
          <div>
            <label className="label">Title</label>
            <input
              required
              className="input"
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
            />
          </div>
          <div>
            <label className="label">Message</label>
            <textarea
              required
              rows={4}
              className="input"
              value={form.message}
              onChange={(e) => setForm({ ...form, message: e.target.value })}
            />
          </div>
          <div>
            <label className="label">Scope</label>
            <select
              className="input"
              value={form.zoneId}
              onChange={(e) => setForm({ ...form, zoneId: e.target.value })}
            >
              <option value="">City-wide (all zones)</option>
              {zones.map((z) => (
                <option key={z.id} value={z.id}>
                  {z.zoneName} only
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Priority</label>
            <select
              className="input"
              value={form.priority}
              onChange={(e) => setForm({ ...form, priority: e.target.value })}
            >
              <option value="NORMAL">Normal</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
        </form>
        <div className="mt-6 flex justify-end gap-3">
          <button className="btn-secondary" onClick={() => setModalOpen(false)}>
            Cancel
          </button>
          <button form="announcement-form" type="submit" disabled={saving} className="btn-primary">
            {saving ? 'Saving…' : 'Publish'}
          </button>
        </div>
      </Modal>
    </DashboardLayout>
  );
}
