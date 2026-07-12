import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader, EmptyState } from '../../components/common/PageHeader.js';
import Loader from '../../components/common/Loader.js';
import Badge from '../../components/common/Badge.js';
import Modal from '../../components/common/Modal.js';
import { complaintApi } from '../../api/complaintApi.js';
import { formatDateTime } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function ComplaintHistory() {
  const { showToast } = useToast();
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    complaintApi
      .getMine()
      .then(({ data }) => setComplaints(data.data || []))
      .catch((err) => showToast(extractErrorMessage(err), 'error'))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <DashboardLayout>
      <PageHeader
        title="My Complaints"
        subtitle="Track the status of complaints you've filed."
        action={
          <Link to="/citizen/complaints/new" className="btn-primary">
            + New Complaint
          </Link>
        }
      />

      {loading ? (
        <Loader />
      ) : complaints.length === 0 ? (
        <EmptyState title="No complaints filed yet" subtitle="Submit one if something needs attention." icon="📮" />
      ) : (
        <div className="space-y-3">
          {complaints.map((c) => (
            <button
              key={c.id}
              onClick={() => setSelected(c)}
              className="card flex w-full items-center justify-between p-4 text-left hover:border-brand-300"
            >
              <div>
                <div className="flex items-center gap-2">
                  <p className="font-semibold text-ink-900">{c.category}</p>
                  <Badge status={c.status} />
                </div>
                <p className="mt-1 line-clamp-1 text-sm text-ink-500">{c.description}</p>
                <p className="mt-1 text-xs text-ink-400">{formatDateTime(c.createdAt)}</p>
              </div>
              <span className="text-ink-300">›</span>
            </button>
          ))}
        </div>
      )}

      <Modal open={!!selected} onClose={() => setSelected(null)} title="Complaint Detail">
        {selected && (
          <div className="space-y-3 text-sm">
            <div className="flex items-center gap-2">
              <span className="font-semibold text-ink-900">{selected.category}</span>
              <Badge status={selected.status} />
            </div>
            <p className="rounded-lg bg-ink-50 p-3 text-ink-700">{selected.description}</p>
            {selected.imageUrls?.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {selected.imageUrls.map((url, idx) => (
                  <img key={idx} src={url} alt="" className="h-20 w-20 rounded-lg object-cover border border-ink-200" />
                ))}
              </div>
            )}
            <p className="text-xs text-ink-400">Filed on {formatDateTime(selected.createdAt)}</p>
            {selected.resolutionRemarks && (
              <div className="rounded-lg bg-brand-50 p-3">
                <p className="text-xs font-semibold text-brand-700">Resolution</p>
                <p className="mt-1 text-brand-800">{selected.resolutionRemarks}</p>
              </div>
            )}
          </div>
        )}
      </Modal>
    </DashboardLayout>
  );
}
