import React from 'react';

export default function Loader({ label = 'Loading…', fullscreen = false }) {
  const content = (
    <div className="flex flex-col items-center justify-center gap-3 py-16">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-200 border-t-brand-600" />
      <p className="text-sm text-ink-500">{label}</p>
    </div>
  );

  if (fullscreen) {
    return <div className="flex h-screen items-center justify-center bg-ink-50">{content}</div>;
  }
  return content;
}
