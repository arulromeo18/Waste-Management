import React from 'react';

export default function AuthLayout({ title, subtitle, children, footer }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-brand-50 via-ink-50 to-ink-100 px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-8 flex flex-col items-center gap-2 text-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-600 text-2xl text-white shadow-card">
            🌿
          </span>
          <h1 className="text-xl font-bold text-ink-900">CleanCity</h1>
          <p className="text-xs uppercase tracking-wide text-ink-400">Waste Management System</p>
        </div>
        <div className="card p-8">
          <h2 className="text-lg font-bold text-ink-900">{title}</h2>
          {subtitle && <p className="mt-1 text-sm text-ink-500">{subtitle}</p>}
          <div className="mt-6">{children}</div>
        </div>
        {footer && <div className="mt-6 text-center text-sm text-ink-500">{footer}</div>}
      </div>
    </div>
  );
}
