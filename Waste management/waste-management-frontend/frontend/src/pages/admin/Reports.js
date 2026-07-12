import React, { useEffect, useState } from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader } from '../../components/common/PageHeader.js';
import { reportApi } from '../../api/dashboardApi.js';
import { zoneApi } from '../../api/zoneApi.js';
import { downloadBlobResponse } from '../../utils/downloadFile.js';
import { todayIso, daysAgoIso } from '../../utils/dateUtil.js';
import { extractErrorMessage, useToast } from '../../components/common/Toast.js';

export default function Reports() {
  const { showToast } = useToast();
  const [zones, setZones] = useState([]);
  const [zoneId, setZoneId] = useState('');
  const [start, setStart] = useState(daysAgoIso(30));
  const [end, setEnd] = useState(todayIso());
  const [downloading, setDownloading] = useState('');

  useEffect(() => {
    zoneApi
      .getAll()
      .then(({ data }) => {
        const list = data.data || [];
        setZones(list);
        if (list.length) setZoneId(list[0].id);
      })
      .catch(() => {});
  }, []);

  async function run(key, fn, filename) {
    setDownloading(key);
    try {
      const response = await fn();
      downloadBlobResponse(response, filename);
      showToast('Download started');
    } catch (err) {
      showToast(extractErrorMessage(err), 'error');
    } finally {
      setDownloading('');
    }
  }

  return (
    <DashboardLayout>
      <PageHeader title="Reports" subtitle="Export system data as CSV, Excel, or PDF." />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <ReportCard title="Dashboard Summary" description="Full dashboard statistics as a formatted PDF.">
          <button
            className="btn-primary"
            disabled={downloading === 'dashboard-pdf'}
            onClick={() => run('dashboard-pdf', reportApi.downloadDashboardPdf, 'dashboard-report.pdf')}
          >
            {downloading === 'dashboard-pdf' ? 'Preparing…' : 'Download PDF'}
          </button>
        </ReportCard>

        <ReportCard title="Citizens" description="All citizen accounts, reward points, and complaint counts.">
          <div className="flex gap-2">
            <button
              className="btn-secondary"
              disabled={downloading === 'citizens-csv'}
              onClick={() => run('citizens-csv', reportApi.exportCitizensCsv, 'citizens.csv')}
            >
              CSV
            </button>
            <button
              className="btn-secondary"
              disabled={downloading === 'citizens-excel'}
              onClick={() => run('citizens-excel', reportApi.exportCitizensExcel, 'citizens.xlsx')}
            >
              Excel
            </button>
          </div>
        </ReportCard>

        <ReportCard title="Workers" description="All sanitation worker accounts and activity.">
          <div className="flex gap-2">
            <button
              className="btn-secondary"
              disabled={downloading === 'workers-csv'}
              onClick={() => run('workers-csv', reportApi.exportWorkersCsv, 'workers.csv')}
            >
              CSV
            </button>
            <button
              className="btn-secondary"
              disabled={downloading === 'workers-excel'}
              onClick={() => run('workers-excel', reportApi.exportWorkersExcel, 'workers.xlsx')}
            >
              Excel
            </button>
          </div>
        </ReportCard>

        <ReportCard title="Complaints" description="Every complaint filed system-wide.">
          <div className="flex gap-2">
            <button
              className="btn-secondary"
              disabled={downloading === 'complaints-csv'}
              onClick={() => run('complaints-csv', reportApi.exportComplaintsCsv, 'complaints.csv')}
            >
              CSV
            </button>
            <button
              className="btn-secondary"
              disabled={downloading === 'complaints-excel'}
              onClick={() => run('complaints-excel', reportApi.exportComplaintsExcel, 'complaints.xlsx')}
            >
              Excel
            </button>
          </div>
        </ReportCard>

        <ReportCard
          title="Collection Records"
          description="Zone-wise collection history within a date range."
          wide
        >
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <div>
              <label className="label">Zone</label>
              <select className="input" value={zoneId} onChange={(e) => setZoneId(e.target.value)}>
                {zones.map((z) => (
                  <option key={z.id} value={z.id}>
                    {z.zoneName}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">From</label>
              <input type="date" className="input" value={start} onChange={(e) => setStart(e.target.value)} />
            </div>
            <div>
              <label className="label">To</label>
              <input type="date" className="input" value={end} onChange={(e) => setEnd(e.target.value)} />
            </div>
          </div>
          <div className="mt-4 flex gap-2">
            <button
              className="btn-secondary"
              disabled={downloading === 'collections-csv' || !zoneId}
              onClick={() =>
                run(
                  'collections-csv',
                  () => reportApi.exportCollectionsCsv({ zoneId, start, end }),
                  'collection-records.csv'
                )
              }
            >
              CSV
            </button>
            <button
              className="btn-secondary"
              disabled={downloading === 'collections-excel' || !zoneId}
              onClick={() =>
                run(
                  'collections-excel',
                  () => reportApi.exportCollectionsExcel({ zoneId, start, end }),
                  'collection-records.xlsx'
                )
              }
            >
              Excel
            </button>
          </div>
        </ReportCard>
      </div>
    </DashboardLayout>
  );
}

function ReportCard({ title, description, children, wide }) {
  return (
    <div className={`card p-5 ${wide ? 'lg:col-span-2' : ''}`}>
      <h3 className="font-semibold text-ink-900">{title}</h3>
      <p className="mt-1 text-sm text-ink-500">{description}</p>
      <div className="mt-4">{children}</div>
    </div>
  );
}
