import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { complaintApi } from '../../api/complaintApi.js';
import { uploadApi } from '../../api/dashboardApi.js';
import { COMPLAINT_CATEGORIES } from '../../utils/constants.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function SubmitComplaint() {
  const { showToast } = useToast();
  const navigate = useNavigate();
  const [form, setForm] = useState({ category: COMPLAINT_CATEGORIES[0], description: '' });
  const [files, setFiles] = useState([]);
  const [previews, setPreviews] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  function handleFileChange(e) {
    const selected = Array.from(e.target.files || []);
    setFiles(selected);
    setPreviews(selected.map((f) => URL.createObjectURL(f)));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      let imageUrls = [];
      if (files.length > 0) {
        const { data } = await uploadApi.uploadComplaintImages(files);
        imageUrls = data.data || [];
      }
      await complaintApi.file({ ...form, imageUrls });
      showToast('Complaint submitted successfully');
      navigate('/citizen/complaints');
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <DashboardLayout>
      <PageHeader title="Submit a Complaint" subtitle="Report a missed pickup or waste-related issue." />

      <div className="mx-auto max-w-xl">
        <form onSubmit={handleSubmit} className="card space-y-5 p-6">
          <div>
            <label className="label">Category</label>
            <select
              className="input"
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
            >
              {COMPLAINT_CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="label">Description</label>
            <textarea
              required
              rows={5}
              className="input"
              placeholder="Describe the issue in detail…"
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>

          <div>
            <label className="label">Attach photos (optional)</label>
            <input type="file" multiple accept="image/*" onChange={handleFileChange} className="input" />
            {previews.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {previews.map((src, idx) => (
                  <img key={idx} src={src} alt="" className="h-20 w-20 rounded-lg object-cover border border-ink-200" />
                ))}
              </div>
            )}
          </div>

          <button type="submit" disabled={submitting} className="btn-primary w-full">
            {submitting ? 'Submitting…' : 'Submit Complaint'}
          </button>
        </form>
      </div>
    </DashboardLayout>
  );
}
