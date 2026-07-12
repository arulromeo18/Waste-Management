import React, { useEffect, useMemo, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import Modal from '../../components/common/Modal.js';
import { vehicleApi } from '../../api/vehicleApi.js';
import { zoneApi } from '../../api/zoneApi.js';
import { VEHICLE_TYPES } from '../../utils/constants.js';
import { formatDate } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

const emptyForm = { vehicleNumber: '', zoneId: '', vehicleType: VEHICLE_TYPES[0], capacity: '' };

export default function ManageVehicles() {
  const { showToast } = useToast();
  const [vehicles, setVehicles] = useState([]);
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [zoneFilter, setZoneFilter] = useState('');

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const [vehiclesRes, zonesRes] = await Promise.all([vehicleApi.getAll(), zoneApi.getAll()]);
      setVehicles(vehiclesRes.data.data || []);
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

  const zoneNameById = useMemo(() => {
    const map = {};
    zones.forEach((z) => (map[z.id] = z.zoneName));
    return map;
  }, [zones]);

  const filtered = vehicles.filter((v) => !zoneFilter || v.zoneId === zoneFilter);

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEdit(vehicle) {
    setEditing(vehicle);
    setForm({
      vehicleNumber: vehicle.vehicleNumber,
      zoneId: vehicle.zoneId,
      vehicleType: vehicle.vehicleType || VEHICLE_TYPES[0],
      capacity: vehicle.capacity || '',
    });
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    try {
      if (editing) {
        await vehicleApi.update(editing.id, form);
        showToast('Vehicle updated successfully');
      } else {
        await vehicleApi.create(form);
        showToast('Vehicle registered successfully');
      }
      setModalOpen(false);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setSaving(false);
    }
  }

  async function toggleActive(vehicle) {
    try {
      await vehicleApi.setActiveStatus(vehicle.id, !vehicle.active);
      showToast(vehicle.active ? 'Vehicle marked under maintenance' : 'Vehicle marked active');
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  async function logMaintenance(vehicle) {
    try {
      await vehicleApi.logMaintenance(vehicle.id);
      showToast('Maintenance logged');
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  return (
    <DashboardLayout>
      <PageHeader
        title="Manage Vehicles"
        subtitle="Fleet of waste collection vehicles by zone."
        action={
          <button className="btn-primary" onClick={openCreate}>
            + Add Vehicle
          </button>
        }
      />

      <select
        className="input mb-4 sm:max-w-xs"
        value={zoneFilter}
        onChange={(e) => setZoneFilter(e.target.value)}
      >
        <option value="">All zones</option>
        {zones.map((z) => (
          <option key={z.id} value={z.id}>
            {z.zoneName}
          </option>
        ))}
      </select>

      {loading ? (
        <Loader />
      ) : filtered.length === 0 ? (
        <EmptyState title="No vehicles found" subtitle="Register your first vehicle." icon="🚛" />
      ) : (
        <div className="table-shell">
          <table className="table-base">
            <thead>
              <tr>
                <th>Vehicle No.</th>
                <th>Zone</th>
                <th>Type</th>
                <th>Capacity</th>
                <th>Last Maintenance</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((v) => (
                <tr key={v.id}>
                  <td className="font-medium text-ink-900">{v.vehicleNumber}</td>
                  <td>{zoneNameById[v.zoneId] || v.zoneName || '—'}</td>
                  <td>{v.vehicleType}</td>
                  <td>{v.capacity || '—'}</td>
                  <td>{formatDate(v.lastMaintenanceDate)}</td>
                  <td>
                    <Badge status={v.active ? 'ACTIVE' : 'INACTIVE'}>
                      {v.active ? 'Active' : 'Under maintenance'}
                    </Badge>
                  </td>
                  <td className="text-right whitespace-nowrap">
                    <button className="btn-ghost text-xs mr-2" onClick={() => openEdit(v)}>
                      Edit
                    </button>
                    <button className="btn-ghost text-xs mr-2" onClick={() => logMaintenance(v)}>
                      Log Maintenance
                    </button>
                    <button
                      className={v.active ? 'btn-danger text-xs' : 'btn-primary text-xs'}
                      onClick={() => toggleActive(v)}
                    >
                      {v.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Vehicle' : 'Add Vehicle'}>
        <form id="vehicle-form" onSubmit={handleSave} className="space-y-4">
          <div>
            <label className="label">Vehicle number</label>
            <input
              required
              className="input"
              value={form.vehicleNumber}
              onChange={(e) => setForm({ ...form, vehicleNumber: e.target.value })}
            />
          </div>
          <div>
            <label className="label">Zone</label>
            <select
              required
              className="input"
              value={form.zoneId}
              onChange={(e) => setForm({ ...form, zoneId: e.target.value })}
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
              <label className="label">Vehicle type</label>
              <select
                className="input"
                value={form.vehicleType}
                onChange={(e) => setForm({ ...form, vehicleType: e.target.value })}
              >
                {VEHICLE_TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Capacity</label>
              <input
                className="input"
                placeholder="e.g. 5 tons"
                value={form.capacity}
                onChange={(e) => setForm({ ...form, capacity: e.target.value })}
              />
            </div>
          </div>
        </form>
        <div className="mt-6 flex justify-end gap-3">
          <button className="btn-secondary" onClick={() => setModalOpen(false)}>
            Cancel
          </button>
          <button form="vehicle-form" type="submit" disabled={saving} className="btn-primary">
            {saving ? 'Saving…' : 'Save Vehicle'}
          </button>
        </div>
      </Modal>
    </DashboardLayout>
  );
}
