import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import Modal from '../../components/common/Modal.js';
import { userApi } from '../../api/userApi.js';
import { penaltyApi } from '../../api/penaltyApi.js';
import { formatDateTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

const emptyForm = { citizenId: '', reason: '', fineAmount: '', collectionRecordId: '', complaintId: '' };

export default function Penalties() {
  const { showToast } = useToast();
  const [citizens, setCitizens] = useState([]);
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(true);

  const [issueOpen, setIssueOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [issuing, setIssuing] = useState(false);

  const [waiveTarget, setWaiveTarget] = useState(null);
  const [waiveReason, setWaiveReason] = useState('');
  const [waiving, setWaiving] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const [citizensRes, pendingRes] = await Promise.all([userApi.getAllCitizens(), penaltyApi.getPending()]);
      setCitizens(citizensRes.data.data || []);
      setPending(pendingRes.data.data || []);
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

  const citizenNameById = citizens.reduce((acc, c) => ({ ...acc, [c.id]: c.fullName }), {});

  async function handleIssue(e) {
    e.preventDefault();
    setIssuing(true);
    try {
      await penaltyApi.issue({
        ...form,
        fineAmount: form.fineAmount ? Number(form.fineAmount) : 0,
        collectionRecordId: form.collectionRecordId || null,
        complaintId: form.complaintId || null,
      });
      showToast('Penalty issued successfully');
      setIssueOpen(false);
      setForm(emptyForm);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setIssuing(false);
    }
  }

  async function handleSettle(penalty) {
    try {
      await penaltyApi.settle(penalty.id);
      showToast('Penalty marked as settled');
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  async function handleWaive(e) {
    e.preventDefault();
    setWaiving(true);
    try {
      await penaltyApi.waive(waiveTarget.id, waiveReason);
      showToast('Penalty waived');
      setWaiveTarget(null);
      setWaiveReason('');
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setWaiving(false);
    }
  }

  return (
    <DashboardLayout>
      <PageHeader
        title="Penalties"
        subtitle="Fines issued to citizens for improper waste segregation or violations."
        action={
          <button className="btn-primary" onClick={() => setIssueOpen(true)}>
            + Issue Penalty
          </button>
        }
      />

      {loading ? (
        <Loader />
      ) : pending.length === 0 ? (
        <EmptyState title="No pending penalties" subtitle="All clear for now." icon="⚠️" />
      ) : (
        <div className="table-shell">
          <table className="table-base">
            <thead>
              <tr>
                <th>Citizen</th>
                <th>Reason</th>
                <th>Fine Amount</th>
                <th>Issued On</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {pending.map((p) => (
                <tr key={p.id}>
                  <td className="font-medium text-ink-900">{citizenNameById[p.citizenId] || p.citizenId}</td>
                  <td>{p.reason}</td>
                  <td>₹{Number(p.fineAmount).toFixed(2)}</td>
                  <td>{formatDateTime(p.createdAt)}</td>
                  <td>
                    <Badge status="PENDING" />
                  </td>
                  <td className="text-right whitespace-nowrap">
                    <button className="btn-secondary text-xs mr-2" onClick={() => setWaiveTarget(p)}>
                      Waive
                    </button>
                    <button className="btn-primary text-xs" onClick={() => handleSettle(p)}>
                      Mark Settled
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={issueOpen} onClose={() => setIssueOpen(false)} title="Issue Penalty">
        <form id="penalty-form" onSubmit={handleIssue} className="space-y-4">
          <div>
            <label className="label">Citizen</label>
            <select
              required
              className="input"
              value={form.citizenId}
              onChange={(e) => setForm({ ...form, citizenId: e.target.value })}
            >
              <option value="">Select citizen</option>
              {citizens.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.fullName}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Reason</label>
            <textarea
              required
              rows={3}
              className="input"
              placeholder="e.g. Repeated improper segregation of wet/dry waste"
              value={form.reason}
              onChange={(e) => setForm({ ...form, reason: e.target.value })}
            />
          </div>
          <div>
            <label className="label">Fine amount (₹)</label>
            <input
              type="number"
              min={0}
              step="0.01"
              required
              className="input"
              value={form.fineAmount}
              onChange={(e) => setForm({ ...form, fineAmount: e.target.value })}
            />
          </div>
        </form>
        <div className="mt-6 flex justify-end gap-3">
          <button className="btn-secondary" onClick={() => setIssueOpen(false)}>
            Cancel
          </button>
          <button form="penalty-form" type="submit" disabled={issuing} className="btn-primary">
            {issuing ? 'Issuing…' : 'Issue Penalty'}
          </button>
        </div>
      </Modal>

      <Modal open={!!waiveTarget} onClose={() => setWaiveTarget(null)} title="Waive Penalty">
        {waiveTarget && (
          <form id="waive-form" onSubmit={handleWaive} className="space-y-4">
            <p className="text-sm text-ink-500">
              Waiving penalty for{' '}
              <span className="font-semibold text-ink-800">{citizenNameById[waiveTarget.citizenId]}</span>
            </p>
            <div>
              <label className="label">Waiver reason</label>
              <textarea
                required
                rows={3}
                className="input"
                value={waiveReason}
                onChange={(e) => setWaiveReason(e.target.value)}
              />
            </div>
          </form>
        )}
        <div className="mt-6 flex justify-end gap-3">
          <button className="btn-secondary" onClick={() => setWaiveTarget(null)}>
            Cancel
          </button>
          <button form="waive-form" type="submit" disabled={waiving} className="btn-primary">
            {waiving ? 'Waiving…' : 'Confirm Waiver'}
          </button>
        </div>
      </Modal>
    </DashboardLayout>
  );
}
