import React, { useEffect, useMemo, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import Modal from '../../components/common/Modal.js';
import { userApi } from '../../api/userApi.js';
import { zoneApi } from '../../api/zoneApi.js';
import { formatDate } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function ManageCitizens() {
  const { showToast } = useToast();
  const [citizens, setCitizens] = useState([]);
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [zoneFilter, setZoneFilter] = useState('');
  const [selected, setSelected] = useState(null);

  async function load() {
    setLoading(true);
    try {
      const [citizensRes, zonesRes] = await Promise.all([userApi.getAllCitizens(), zoneApi.getAll()]);
      setCitizens(citizensRes.data.data || []);
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

  const filtered = citizens.filter((c) => {
    const matchesSearch =
      c.fullName.toLowerCase().includes(search.toLowerCase()) ||
      c.email.toLowerCase().includes(search.toLowerCase());
    const matchesZone = !zoneFilter || c.zoneId === zoneFilter;
    return matchesSearch && matchesZone;
  });

  async function toggleActive(citizen) {
    try {
      await userApi.setUserActiveStatus(citizen.id, !citizen.active);
      showToast(`${citizen.fullName} ${citizen.active ? 'deactivated' : 'activated'}`);
      load();
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    }
  }

  return (
    <DashboardLayout>
      <PageHeader title="Manage Citizens" subtitle="All registered citizen accounts across zones." />

      <div className="mb-4 flex flex-col gap-3 sm:flex-row">
        <input
          className="input sm:max-w-xs"
          placeholder="Search by name or email…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
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
      ) : filtered.length === 0 ? (
        <EmptyState title="No citizens found" subtitle="Try adjusting your search or filter." icon="🧑‍🤝‍🧑" />
      ) : (
        <div className="table-shell">
          <table className="table-base">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Zone</th>
                <th>Reward Points</th>
                <th>Complaints Filed</th>
                <th>Status</th>
                <th>Registered</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((c) => (
                <tr key={c.id}>
                  <td className="font-medium text-ink-900">{c.fullName}</td>
                  <td>{c.email}</td>
                  <td>{c.phone}</td>
                  <td>{zoneNameById[c.zoneId] || '—'}</td>
                  <td>{c.rewardPoints ?? 0}</td>
                  <td>{c.totalComplaintsFiled ?? 0}</td>
                  <td>
                    <Badge status={c.active ? 'ACTIVE' : 'INACTIVE'}>{c.active ? 'Active' : 'Inactive'}</Badge>
                  </td>
                  <td>{formatDate(c.createdAt)}</td>
                  <td className="text-right">
                    <button className="btn-ghost text-xs mr-2" onClick={() => setSelected(c)}>
                      View
                    </button>
                    <button
                      className={c.active ? 'btn-danger text-xs' : 'btn-primary text-xs'}
                      onClick={() => toggleActive(c)}
                    >
                      {c.active ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={!!selected} onClose={() => setSelected(null)} title="Citizen Details">
        {selected && (
          <div className="space-y-2 text-sm">
            <Row label="Full name" value={selected.fullName} />
            <Row label="Email" value={selected.email} />
            <Row label="Phone" value={selected.phone} />
            <Row label="Zone" value={zoneNameById[selected.zoneId] || '—'} />
            <Row label="Reward points" value={selected.rewardPoints ?? 0} />
            <Row label="Complaints filed" value={selected.totalComplaintsFiled ?? 0} />
            <Row label="Status" value={selected.active ? 'Active' : 'Inactive'} />
            <Row label="Registered on" value={formatDate(selected.createdAt)} />
          </div>
        )}
      </Modal>
    </DashboardLayout>
  );
}

function Row({ label, value }) {
  return (
    <div className="flex justify-between border-b border-ink-100 py-2">
      <span className="text-ink-500">{label}</span>
      <span className="font-medium text-ink-800">{value}</span>
    </div>
  );
}
