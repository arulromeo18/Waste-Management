import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ToastProvider } from './components/common/Toast.js';
import ProtectedRoute from './routes/ProtectedRoute.js';
import { useAuth } from './context/AuthContext.js';
import { roleHome } from './routes/ProtectedRoute.js';

import Login from './pages/auth/Login.js';
import Register from './pages/auth/Register.js';
import ForgotPassword from './pages/auth/ForgotPassword.js';
import ResetPassword from './pages/auth/ResetPassword.js';

import AdminDashboard from './pages/admin/AdminDashboard.js';
import ManageCitizens from './pages/admin/ManageCitizens.js';
import ManageWorkers from './pages/admin/ManageWorkers.js';
import ManageVehicles from './pages/admin/ManageVehicles.js';
import ManageZones from './pages/admin/ManageZones.js';
import ManageSchedules from './pages/admin/ManageSchedules.js';
import ComplaintsView from './pages/admin/ComplaintsView.js';
import Reports from './pages/admin/Reports.js';
import Announcements from './pages/admin/Announcements.js';
import Rewards from './pages/admin/Rewards.js';
import Penalties from './pages/admin/Penalties.js';

import WorkerDashboard from './pages/worker/WorkerDashboard.js';
import TodaySchedule from './pages/worker/TodaySchedule.js';
import AssignedRoute from './pages/worker/AssignedRoute.js';
import UploadWasteImages from './pages/worker/UploadWasteImages.js';

import CitizenDashboard from './pages/citizen/CitizenDashboard.js';
import Profile from './pages/citizen/Profile.js';
import ScheduleView from './pages/citizen/ScheduleView.js';
import SubmitComplaint from './pages/citizen/SubmitComplaint.js';
import ComplaintHistory from './pages/citizen/ComplaintHistory.js';
import SegregationGuide from './pages/citizen/SegregationGuide.js';
import CitizenRewards from './pages/citizen/CitizenRewards.js';

import NotificationsPage from './pages/common/NotificationsPage.js';
import NotFound from './pages/common/NotFound.js';

function RootRedirect() {
  const { user, initializing } = useAuth();
  if (initializing) return null;
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={roleHome(user.role)} replace />;
}

export default function App() {
  return (
    <ToastProvider>
      <Routes>
        <Route path="/" element={<RootRedirect />} />

        {/* Public auth routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        {/* Super Admin routes */}
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/citizens"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <ManageCitizens />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/workers"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <ManageWorkers />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/vehicles"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <ManageVehicles />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/zones"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <ManageZones />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/schedules"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <ManageSchedules />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/complaints"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <ComplaintsView />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/announcements"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <Announcements />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/rewards"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <Rewards />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/penalties"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <Penalties />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/reports"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <Reports />
            </ProtectedRoute>
          }
        />

        {/* Worker routes */}
        <Route
          path="/worker/dashboard"
          element={
            <ProtectedRoute allowedRoles={['WORKER']}>
              <WorkerDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/worker/today-schedule"
          element={
            <ProtectedRoute allowedRoles={['WORKER']}>
              <TodaySchedule />
            </ProtectedRoute>
          }
        />
        <Route
          path="/worker/assigned-route"
          element={
            <ProtectedRoute allowedRoles={['WORKER']}>
              <AssignedRoute />
            </ProtectedRoute>
          }
        />
        <Route
          path="/worker/upload"
          element={
            <ProtectedRoute allowedRoles={['WORKER']}>
              <UploadWasteImages />
            </ProtectedRoute>
          }
        />

        {/* Citizen routes */}
        <Route
          path="/citizen/dashboard"
          element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <CitizenDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/profile"
          element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <Profile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/schedule"
          element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <ScheduleView />
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/complaints/new"
          element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <SubmitComplaint />
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/complaints"
          element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <ComplaintHistory />
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/rewards"
          element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <CitizenRewards />
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/segregation-guide"
          element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <SegregationGuide />
            </ProtectedRoute>
          }
        />
        <Route
          path="/citizen/notifications"
          element={
            <ProtectedRoute allowedRoles={['CITIZEN']}>
              <NotificationsPage />
            </ProtectedRoute>
          }
        />

        {/* Shared notifications route for admin + worker */}
        <Route
          path="/notifications"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN', 'WORKER']}>
              <NotificationsPage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<NotFound />} />
      </Routes>
    </ToastProvider>
  );
}
