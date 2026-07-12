import React, { useEffect, useMemo, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import Modal from '../../components/common/Modal.js';
import { userApi } from '../../api/userApi.js';
import { zoneApi } from '../../api/zoneApi.js';
import { vehicleApi } from '../../api/vehicleApi.js';
import { formatDate } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

const emptyCreateForm = {
  fullName: '',
  email: '',
  password: '',
  phone: '',
  zoneId: '',
  employeeId: '',
  shiftTiming: '',
};

export default function ManageWorkers() {
  const { showToast } = useToast();
  const [workers, setWorkers] = useState([]);
  const [zones, setZones] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState(emptyCreateForm);
  const [creating, setCreating] = useState(false);

  const [assignTarget, setAssignTarget] = useState(null);
  const [assignForm, setAssignForm] = useState({ zoneId: '', vehicleId: '' });
  const [assigning, setAssigning] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const [workersRes, zonesRes, vehiclesRes] = await Promise.all([
        userApi.getAllWorkers(),
        zoneApi.getAll(),
        vehicleApi.getAll(),
      ]);
      setWorkers(workersRes.data.data || []);
      setZones(zonesRes.data.data || []);
      setVehicles(vehiclesRes.data.data || []);
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

  const zoneNameById = useMemo(() => {
    const map = {};
    zones.forEach((z) => (map[z.id] = z.zoneName));
    return map;
  }, [zones]);

  const vehicleNumberById = useMemo(() => {
    const map = {};
    vehicles.forEach((v) => (map[v.id] = v.vehicleNumber));
    return map;
  }, [vehicles]);

  const filtered = workers.filter(
    (w) =>
      w.fullName.toLowerCase().includes(search.toLowerCase()) ||
      w.email.toLowerCase().includes(search.toLowerCase())
  );

  async function toggleActive(worker) {
    try {
      await userApi.setUserActiveStatus(worker.id, !worker.active);
      showToast(`${worker.fullName} ${worker.active ? 'deactivated' : 'activated'}`);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  async function handleCreate(e) {
    e.preventDefault();
    setCreating(true);
    try {
      await userApi.createWorker(createForm);
      showToast('Worker account created successfully');
      setCreateOpen(false);
      setCreateForm(emptyCreateForm);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setCreating(false);
    }
  }

  function openAssign(worker) {
    setAssignTarget(worker);
    setAssignForm({ zoneId: worker.zoneId || '', vehicleId: worker.vehicleId || '' });
  }

  async function handleAssign(e) {
    e.preventDefault();
    setAssigning(true);
    try {
      await userApi.assignWorker(assignTarget.id, assignForm);
      showToast('Worker assignment updated');
      setAssignTarget(null);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setAssigning(false);
    }
  }

  return (
    <DashboardLayout>
      <PageHeader
        title="Manage Workers"
        subtitle="Sanitation worker accounts, zone assignment, and vehicle allocation."
        action={
          <button className="btn-primary" onClick={() => setCreateOpen(true)}>
            + Add Worker
          </button>
        }
      />

      <input
        className="input mb-4 sm:max-w-xs"
        placeholder="Search by name or email…"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      {loading ? (
        <Loader />
      ) : filtered.length === 0 ? (
        <EmptyState title="No workers found" subtitle="Add your first sanitation worker." icon="🧹" />
      ) : (
        <div className="table-shell">
          <table className="table-base">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Zone</th>
                <th>Vehicle</th>
                <th>Employee ID</th>
                <th>Shift</th>
                <th>Collections Logged</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((w) => (
                <tr key={w.id}>
                  <td className="font-medium text-ink-900">{w.fullName}</td>
                  <td>{w.email}</td>
                  <td>{zoneNameById[w.zoneId] || '—'}</td>
                  <td>{vehicleNumberById[w.vehicleId] || '—'}</td>
                  <td>{w.employeeId || '—'}</td>
                  <td>{w.shiftTiming || '—'}</td>
                  <td>{w.totalCollectionsLogged ?? 0}</td>
                  <td>
                    <Badge status={w.active ? 'ACTIVE' : 'INACTIVE'}>{w.active ? 'Active' : 'Inactive'}</Badge>
                  </td>
                  <td className="text-right whitespace-nowrap">
                    <button className="btn-ghost text-xs mr-2" onClick={() => openAssign(w)}>
                      Assign
                    </button>
                    <button
                      className={w.active ? 'btn-danger text-xs' : 'btn-primary text-xs'}
                      onClick={() => toggleActive(w)}
                    >
                      {w.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={createOpen} onClose={() => setCreateOpen(false)} title="Add New Worker">
        <form id="create-worker-form" onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="label">Full name</label>
            <input
              required
              className="input"
              value={createForm.fullName}
              onChange={(e) => setCreateForm({ ...createForm, fullName: e.target.value })}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Email</label>
              <input
                type="email"
                required
                className="input"
                value={createForm.email}
                onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
              />
            </div>
            <div>
              <label className="label">Phone</label>
              <input
                required
                className="input"
                value={createForm.phone}
                onChange={(e) => setCreateForm({ ...createForm, phone: e.target.value })}
              />
            </div>
          </div>
          <div>
            <label className="label">Temporary password</label>
            <input
              type="password"
              required
              minLength={6}
              className="input"
              value={createForm.password}
              onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
            />
          </div>
          <div>
            <label className="label">Zone</label>
            <select
              required
              className="input"
              value={createForm.zoneId}
              onChange={(e) => setCreateForm({ ...createForm, zoneId: e.target.value })}
            >
              <option value="">Select zone</option>
              {zones.map((z) => (
                <option key={z.id} value={z.id}>
                  {z.zoneName}
                </option>
              ))}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Employee ID</label>
              <input
                className="input"
                value={createForm.employeeId}
                onChange={(e) => setCreateForm({ ...createForm, employeeId: e.target.value })}
              />
            </div>
            <div>
              <label className="label">Shift timing</label>
              <input
                className="input"
                placeholder="e.g. 06:00 - 14:00"
                value={createForm.shiftTiming}
                onChange={(e) => setCreateForm({ ...createForm, shiftTiming: e.target.value })}
              />
            </div>
          </div>
        </form>
        <div className="mt-6 flex justify-end gap-3">
          <button className="btn-secondary" onClick={() => setCreateOpen(false)}>
            Cancel
          </button>
          <button form="create-worker-form" type="submit" disabled={creating} className="btn-primary">
            {creating ? 'Creating…' : 'Create Worker'}
          </button>
        </div>
      </Modal>

      <Modal open={!!assignTarget} onClose={() => setAssignTarget(null)} title="Assign Worker">
        {assignTarget && (
          <form id="assign-worker-form" onSubmit={handleAssign} className="space-y-4">
            <p className="text-sm text-ink-500">
              Assigning <span className="font-semibold text-ink-800">{assignTarget.fullName}</span>
            </p>
            <div>
              <label className="label">Zone</label>
              <select
                required
                className="input"
                value={assignForm.zoneId}
                onChange={(e) => setAssignForm({ ...assignForm, zoneId: e.target.value })}
              >
                <option value="">Select zone</option>
                {zones.map((z) => (
                  <option key={z.id} value={z.id}>
                    {z.zoneName}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Vehicle (optional)</label>
              <select
                className="input"
                value={assignForm.vehicleId}
                onChange={(e) => setAssignForm({ ...assignForm, vehicleId: e.target.value })}
              >
                <option value="">No vehicle</option>
                {vehicles
                  .filter((v) => v.zoneId === assignForm.zoneId)
                  .map((v) => (
                    <option key={v.id} value={v.id}>
                      {v.vehicleNumber}
                    </option>
                  ))}
              </select>
            </div>
          </form>
        )}
        <div className="mt-6 flex justify-end gap-3">
          <button className="btn-secondary" onClick={() => setAssignTarget(null)}>
            Cancel
          </button>
          <button form="assign-worker-form" type="submit" disabled={assigning} className="btn-primary">
            {assigning ? 'Saving…' : 'Save Assignment'}
          </button>
        </div>
      </Modal>
    </DashboardLayout>
  );
}
