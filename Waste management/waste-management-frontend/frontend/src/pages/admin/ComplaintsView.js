import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import Modal from '../../components/common/Modal.js';
import { complaintApi } from '../../api/complaintApi.js';
import { zoneApi } from '../../api/zoneApi.js';
import { COMPLAINT_STATUSES } from '../../utils/constants.js';
import { formatDateTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function ComplaintsView() {
  const { showToast } = useToast();
  const [complaints, setComplaints] = useState([]);
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('PENDING');
  const [zoneFilter, setZoneFilter] = useState('');

  const [selected, setSelected] = useState(null);
  const [remarks, setRemarks] = useState('');
  const [updating, setUpdating] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const params = {};
      if (statusFilter) params.status = statusFilter;
      if (zoneFilter) params.zoneId = zoneFilter;
      const [complaintsRes, zonesRes] = await Promise.all([
        complaintApi.getForAdmin(params),
        zones.length ? Promise.resolve({ data: { data: zones } }) : zoneApi.getAll(),
      ]);
      setComplaints(complaintsRes.data.data || []);
      if (!zones.length) setZones(zonesRes.data.data || []);
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter, zoneFilter]);

  function openDetail(complaint) {
    setSelected(complaint);
    setRemarks(complaint.resolutionRemarks || '');
  }

  async function updateStatus(newStatus) {
    if ((newStatus === 'RESOLVED' || newStatus === 'REJECTED') && !remarks.trim()) {
      showToast('Resolution remarks are required to close a complaint', 'error');
      return;
    }
    setUpdating(true);
    try {
      await complaintApi.updateStatus(selected.id, newStatus, remarks);
      showToast('Complaint status updated');
      setSelected(null);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setUpdating(false);
    }
  }

  return (
    <DashboardLayout>
      <PageHeader title="Complaints" subtitle="Review and resolve citizen-filed complaints." />

      <div className="mb-4 flex flex-col gap-3 sm:flex-row">
        <select className="input sm:max-w-xs" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">All statuses</option>
          {COMPLAINT_STATUSES.map((s) => (
            <option key={s} value={s}>
              {s.replace('_', ' ')}
            </option>
          ))}
        </select>
        <select className="input sm:max-w-xs" value={zoneFilter} onChange={(e) => setZoneFilter(e.target.value)}>
          <option value="">All zones</option>
          {zones.map((z) => (
            <option key={z.id} value={z.id}>
              {z.zoneName}
            </option>
          ))}
        </select>
      </div>

      {loading ? (
        <Loader />
      ) : complaints.length === 0 ? (
        <EmptyState title="No complaints found" subtitle="Nothing matches this filter." icon="📮" />
      ) : (
        <div className="table-shell">
          <table className="table-base">
            <thead>
              <tr>
                <th>Citizen</th>
                <th>Zone</th>
                <th>Category</th>
                <th>Filed On</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {complaints.map((c) => (
                <tr key={c.id}>
                  <td className="font-medium text-ink-900">{c.citizenName}</td>
                  <td>{c.zoneName}</td>
                  <td>{c.category}</td>
                  <td>{formatDateTime(c.createdAt)}</td>
                  <td>
                    <Badge status={c.status} />
                  </td>
                  <td className="text-right">
                    <button className="btn-ghost text-xs" onClick={() => openDetail(c)}>
                      Review
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={!!selected} onClose={() => setSelected(null)} title="Complaint Detail" size="lg">
        {selected && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <Field label="Citizen" value={selected.citizenName} />
              <Field label="Zone" value={selected.zoneName} />
              <Field label="Category" value={selected.category} />
              <Field label="Status">
                <Badge status={selected.status} />
              </Field>
              <Field label="Worker (if any)" value={selected.workerName || '—'} />
              <Field label="Filed on" value={formatDateTime(selected.createdAt)} />
            </div>
            <div>
              <p className="label">Description</p>
              <p className="rounded-lg bg-ink-50 p-3 text-sm text-ink-700">{selected.description}</p>
            </div>
            {selected.imageUrls?.length > 0 && (
              <div>
                <p className="label">Attached photos</p>
                <div className="flex flex-wrap gap-2">
                  {selected.imageUrls.map((url, idx) => (
                    <img
                      key={idx}
                      src={url}
                      alt={`Evidence ${idx + 1}`}
                      className="h-20 w-20 rounded-lg object-cover border border-ink-200"
                    />
                  ))}
                </div>
              </div>
            )}
            <div>
              <label className="label">Resolution remarks</label>
              <textarea
                className="input"
                rows={3}
                placeholder="Required when resolving or rejecting…"
                value={remarks}
                onChange={(e) => setRemarks(e.target.value)}
              />
            </div>
          </div>
        )}
        <div className="mt-6 flex flex-wrap justify-end gap-2">
          <button disabled={updating} className="btn-secondary" onClick={() => updateStatus('IN_PROGRESS')}>
            Mark In Progress
          </button>
          <button disabled={updating} className="btn-danger" onClick={() => updateStatus('REJECTED')}>
            Reject
          </button>
          <button disabled={updating} className="btn-primary" onClick={() => updateStatus('RESOLVED')}>
            Resolve
          </button>
        </div>
      </Modal>
    </DashboardLayout>
  );
}

function Field({ label, value, children }) {
  return (
    <div>
      <p className="text-xs text-ink-400">{label}</p>
      {children || <p className="font-medium text-ink-800">{value}</p>}
    </div>
  );
}
