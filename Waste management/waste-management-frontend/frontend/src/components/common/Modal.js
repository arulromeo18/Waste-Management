import React from 'react';

export default function Modal({ open, onClose, title, children, footer, size = 'md' }) {
  if (!open) return null;

  const widths = {
    sm: 'max-w-sm',
    md: 'max-w-lg',
    lg: 'max-w-2xl',
    xl: 'max-w-4xl',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink-900/40 p-4">
      <div className={`w-full ${widths[size]} rounded-2xl bg-white shadow-xl`}>
        <div className="flex items-center justify-between border-b border-ink-100 px-6 py-4">
          <h3 className="text-base font-semibold text-ink-900">{title}</h3>
          <button
            onClick={onClose}
            className="rounded-full p-1 text-ink-400 hover:bg-ink-100 hover:text-ink-600"
            aria-label="Close"
          >
            ✕
          </button>
        </div>
        <div className="max-h-[70vh] overflow-y-auto px-6 py-5">{children}</div>
        {footer && <div className="flex justify-end gap-3 border-t border-ink-100 px-6 py-4">{footer}</div>}
      </div>
    </div>
  );
}
