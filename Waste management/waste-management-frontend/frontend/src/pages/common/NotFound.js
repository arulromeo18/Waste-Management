import React from 'react';
import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-ink-50 text-center">
      <span className="text-5xl">🧭</span>
      <h1 className="text-2xl font-bold text-ink-900">Page not found</h1>
      <p className="text-sm text-ink-500">The page you're looking for doesn't exist.</p>
      <Link to="/" className="btn-primary">
        Go home
      </Link>
    </div>
  );
}
