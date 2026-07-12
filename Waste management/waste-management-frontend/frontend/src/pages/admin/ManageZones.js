import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import Modal from '../../components/common/Modal.js';
import { zoneApi } from '../../api/zoneApi.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

const emptyForm = {
  zoneName: '',
  zoneCode: '',
  wardNumber: '',
  description: '',
  estimatedPopulation: '',
  coveredAreas: '',
};

export default function ManageZones() {
  const { showToast } = useToast();
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const { data } = await zoneApi.getAll();
      setZones(data.data || []);
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

  function openEdit(zone) {
    setEditing(zone);
    setForm({
      zoneName: zone.zoneName,
      zoneCode: zone.zoneCode,
      wardNumber: zone.wardNumber || '',
      description: zone.description || '',
      estimatedPopulation: zone.estimatedPopulation || '',
      coveredAreas: (zone.coveredAreas || []).join(', '),
    });
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    const payload = {
      ...form,
      estimatedPopulation: form.estimatedPopulation ? Number(form.estimatedPopulation) : null,
      coveredAreas: form.coveredAreas
        ? form.coveredAreas.split(',').map((s) => s.trim()).filter(Boolean)
        : [],
    };
    try {
      if (editing) {
        await zoneApi.update(editing.id, payload);
        showToast('Zone updated successfully');
      } else {
        await zoneApi.create(payload);
        showToast('Zone created successfully');
      }
      setModalOpen(false);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setSaving(false);
    }
  }

  async function toggleActive(zone) {
    try {
      await zoneApi.setActiveStatus(zone.id, !zone.active);
      showToast(zone.active ? 'Zone deactivated' : 'Zone activated');
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  return (
    <DashboardLayout>
      <PageHeader
        title="Manage Zones"
        subtitle="Geographic zones used to organize collection, schedules, and staff."
        action={
          <button className="btn-primary" onClick={openCreate}>
            + Add Zone
          </button>
        }
      />

      {loading ? (
        <Loader />
      ) : zones.length === 0 ? (
        <EmptyState title="No zones found" subtitle="Create your first zone to get started." icon="🗺️" />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {zones.map((z) => (
            <div key={z.id} className="card p-5">
              <div className="mb-2 flex items-start justify-between">
                <div>
                  <h3 className="font-semibold text-ink-900">{z.zoneName}</h3>
                  <p className="text-xs text-ink-400">Code: {z.zoneCode}</p>
                </div>
                <Badge status={z.active ? 'ACTIVE' : 'INACTIVE'}>{z.active ? 'Active' : 'Inactive'}</Badge>
              </div>
              {z.wardNumber && <p className="text-sm text-ink-500">Ward {z.wardNumber}</p>}
              {z.description && <p className="mt-2 text-sm text-ink-600">{z.description}</p>}
              {z.estimatedPopulation && (
                <p className="mt-2 text-xs text-ink-400">
                  Est. population: {z.estimatedPopulation.toLocaleString()}
                </p>
              )}
              {z.coveredAreas?.length > 0 && (
                <div className="mt-3 flex flex-wrap gap-1.5">
                  {z.coveredAreas.map((area) => (
                    <span key={area} className="rounded-full bg-ink-100 px-2 py-0.5 text-xs text-ink-600">
                      {area}
                    </span>
                  ))}
                </div>
              )}
              <div className="mt-4 flex gap-2">
                <button className="btn-secondary flex-1 text-xs" onClick={() => openEdit(z)}>
                  Edit
                </button>
                <button
                  className={`${z.active ? 'btn-danger' : 'btn-primary'} flex-1 text-xs`}
                  onClick={() => toggleActive(z)}
                >
                  {z.active ? 'Deactivate' : 'Activate'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Zone' : 'Add Zone'}>
        <form id="zone-form" onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Zone name</label>
              <input
                required
                className="input"
                value={form.zoneName}
                onChange={(e) => setForm({ ...form, zoneName: e.target.value })}
              />
            </div>
            <div>
              <label className="label">Zone code</label>
              <input
                required
                className="input"
                value={form.zoneCode}
                onChange={(e) => setForm({ ...form, zoneCode: e.target.value })}
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Ward number</label>
              <input
                className="input"
                value={form.wardNumber}
                onChange={(e) => setForm({ ...form, wardNumber: e.target.value })}
              />
            </div>
            <div>
              <label className="label">Estimated population</label>
              <input
                type="number"
                className="input"
                value={form.estimatedPopulation}
                onChange={(e) => setForm({ ...form, estimatedPopulation: e.target.value })}
              />
            </div>
          </div>
          <div>
            <label className="label">Description</label>
            <textarea
              className="input"
              rows={3}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>
          <div>
            <label className="label">Covered areas (comma-separated)</label>
            <input
              className="input"
              placeholder="e.g. MG Road, RS Puram, Gandhipuram"
              value={form.coveredAreas}
              onChange={(e) => setForm({ ...form, coveredAreas: e.target.value })}
            />
          </div>
        </form>
        <div className="mt-6 flex justify-end gap-3">
          <button className="btn-secondary" onClick={() => setModalOpen(false)}>
            Cancel
          </button>
          <button form="zone-form" type="submit" disabled={saving} className="btn-primary">
            {saving ? 'Saving…' : 'Save Zone'}
          </button>
        </div>
      </Modal>
    </DashboardLayout>
  );
}
