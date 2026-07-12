# CleanCity — Waste Management System (Frontend)

React + Vite frontend for the Waste Segregation & Collection Management System, covering all three roles: **Super Admin**, **Sanitation Worker**, and **Citizen**.

## Tech Stack

- React 18 + React Router v6
- Vite (dev server + build)
- Tailwind CSS
- Axios (with JWT interceptor)
- Recharts (dashboard charts)

## Getting Started

```bash
npm install
npm run dev
```

The dev server runs on `http://localhost:5173` and proxies all `/api/**` requests to the Spring Boot backend on `http://localhost:8080` (see `vite.config.js`). Make sure the backend is running first.

To build for production:

```bash
npm run build
```

Output is written to `dist/`. Serve `dist/` behind the same origin/reverse proxy as the backend (or update the API base URL) so relative `/api/...` calls resolve correctly.

## Project Structure

```
src/
├── api/            Axios modules, one per backend controller
├── components/
│   ├── common/     Navbar, Sidebar, Modal, Toast, Badge, Loader, DashboardLayout, PageHeader
│   ├── admin/       (reserved for admin-only sub-components)
│   ├── worker/      (reserved for worker-only sub-components)
│   └── citizen/     (reserved for citizen-only sub-components)
├── context/        AuthContext (JWT session, login/register/logout)
├── pages/
│   ├── auth/       Login, Register, ForgotPassword, ResetPassword
│   ├── admin/      All Super Admin screens
│   ├── worker/     All Sanitation Worker screens
│   ├── citizen/    All Citizen screens
│   └── common/     Shared Notifications page, 404
├── routes/         ProtectedRoute (auth + role gating)
├── styles/         global.css (Tailwind + shared component classes)
└── utils/          constants, date formatting, blob download helper
```

## Role-Based Routing

`AuthContext` stores the JWT and a minimal user object (`userId`, `fullName`, `email`, `role`) in `localStorage`. `ProtectedRoute` redirects unauthenticated users to `/login` and redirects authenticated users away from routes outside their role. `App.js` wires every page behind the correct role guard.

## Known Backend Gap

`pages/citizen/Profile.js` is wired against `GET /api/citizen/profile` and `PATCH /api/citizen/profile` (see `api/citizenProfileApi.js` for details). At the time this frontend was built, no self-service "view/update my own profile" endpoint existed for a Citizen in the reviewed backend controllers — only the Super-Admin-scoped `GET /api/admin/users/{userId}` was available. `ProfileUpdateRequest.java` already exists in the backend DTOs and is otherwise unused, suggesting this route was planned. Add a small controller method (scoped to the authenticated citizen via `@AuthenticationPrincipal`) to make this page fully functional — everything else in the app is wired against endpoints confirmed to exist in the backend.

## Environment

No `.env` is required for local development — the Vite proxy handles routing to the backend. If you deploy the frontend separately from the backend, set `VITE_API_BASE_URL` and update `src/api/axiosInstance.js`'s `baseURL` accordingly.
