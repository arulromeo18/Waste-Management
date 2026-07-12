/**
 * Triggers a browser download from an Axios blob response. Report/export
 * endpoints (see api/dashboardApi.js's reportApi) return raw bytes with a
 * Content-Disposition header set server-side, but Axios doesn't read that
 * header into the filename automatically — so each call site passes the
 * filename it already knows (matching what ReportController names the file).
 */
export function downloadBlobResponse(response, filename) {
  const blob = new Blob([response.data]);
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}
