import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { scheduleApi } from '../../api/scheduleApi.js';
import { collectionRecordApi } from '../../api/collectionRecordApi.js';
import { uploadApi } from '../../api/dashboardApi.js';
import { todayIso } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function UploadWasteImages() {
  const { showToast } = useToast();
  const [schedules, setSchedules] = useState([]);
  const [form, setForm] = useState({
    scheduleId: '',
    collectionDate: todayIso(),
    segregationCompliant: true,
    remarks: '',
  });
  const [files, setFiles] = useState([]);
  const [previews, setPreviews] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    scheduleApi
      .getMySchedule(false)
      .then(({ data }) => setSchedules(data.data || []))
      .catch(() => setSchedules([]));
  }, []);

  function handleFileChange(e) {
    const selected = Array.from(e.target.files || []);
    setFiles(selected);
    setPreviews(selected.map((f) => URL.createObjectURL(f)));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (!form.scheduleId) {
      showToast('Select a zone/schedule for this pickup', 'error');
      return;
    }
    setSubmitting(true);
    try {
      let imageUrls = [];
      if (files.length > 0) {
        const { data } = await uploadApi.uploadCollectionImages(files);
        imageUrls = data.data || [];
      }
      const selectedSchedule = schedules.find((s) => s.id === form.scheduleId);
      await collectionRecordApi.logCollection({
        scheduleId: form.scheduleId,
        zoneId: selectedSchedule?.zoneId,
        collectionDate: form.collectionDate,
        segregationCompliant: form.segregationCompliant,
        remarks: form.remarks,
        imageUrls,
      });
      showToast('Collection logged successfully');
      setForm({ scheduleId: '', collectionDate: todayIso(), segregationCompliant: true, remarks: '' });
      setFiles([]);
      setPreviews([]);
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <DashboardLayout>
      <PageHeader title="Log Collection" subtitle="Record a completed pickup with proof-of-collection photos." />

      <div className="mx-auto max-w-xl">
        <form onSubmit={handleSubmit} className="card space-y-5 p-6">
          <div>
            <label className="label">Zone / Schedule</label>
            <select
              required
              className="input"
              value={form.scheduleId}
              onChange={(e) => setForm({ ...form, scheduleId: e.target.value })}
            >
              <option value="">Select schedule</option>
              {schedules.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.zoneName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="label">Collection date</label>
            <input
              type="date"
              required
              className="input"
              value={form.collectionDate}
              onChange={(e) => setForm({ ...form, collectionDate: e.target.value })}
            />
          </div>

          <div>
            <label className="label">Segregation compliance</label>
            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => setForm({ ...form, segregationCompliant: true })}
                className={`flex-1 rounded-lg border px-4 py-2.5 text-sm font-medium ${
                  form.segregationCompliant
                    ? 'border-brand-500 bg-brand-50 text-brand-700'
                    : 'border-ink-200 text-ink-500'
                }`}
              >
                ✅ Properly Segregated
              </button>
              <button
                type="button"
                onClick={() => setForm({ ...form, segregationCompliant: false })}
                className={`flex-1 rounded-lg border px-4 py-2.5 text-sm font-medium ${
                  !form.segregationCompliant
                    ? 'border-red-500 bg-red-50 text-red-700'
                    : 'border-ink-200 text-ink-500'
                }`}
              >
                ⚠️ Not Segregated
              </button>
            </div>
          </div>

          <div>
            <label className="label">Proof-of-collection photos</label>
            <input type="file" multiple accept="image/*" onChange={handleFileChange} className="input" />
            {previews.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {previews.map((src, idx) => (
                  <img key={idx} src={src} alt="" className="h-20 w-20 rounded-lg object-cover border border-ink-200" />
                ))}
              </div>
            )}
          </div>

          <div>
            <label className="label">Remarks (optional)</label>
            <textarea
              className="input"
              rows={3}
              value={form.remarks}
              onChange={(e) => setForm({ ...form, remarks: e.target.value })}
            />
          </div>

          <button type="submit" disabled={submitting} className="btn-primary w-full">
            {submitting ? 'Submitting…' : 'Submit Collection Record'}
          </button>
        </form>
      </div>
    </DashboardLayout>
  );
}
