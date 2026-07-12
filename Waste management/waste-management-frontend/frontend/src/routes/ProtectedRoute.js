import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.js';

/**
 * Gate for any route that requires a logged-in user, optionally restricted
 * to a specific set of roles. Unauthenticated users are sent to /login;
 * authenticated users whose role isn't in `allowedRoles` are redirected to
 * their own dashboard root rather than shown a blank/broken page.
 */
export default function ProtectedRoute({ children, allowedRoles }) {
  const { user, initializing } = useAuth();

  if (initializing) {
    return (
      <div className="flex h-screen items-center justify-center bg-ink-50">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-200 border-t-brand-600" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to={roleHome(user.role)} replace />;
  }

  return children;
}

export function roleHome(role) {
  if (role === 'SUPER_ADMIN') return '/admin/dashboard';
  if (role === 'WORKER') return '/worker/dashboard';
  return '/citizen/dashboard';
}
