// frontend/src/App.tsx

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './features/auth/LoginPage';
import RegisterPage from './features/auth/RegisterPage';
import DashboardPage from './features/orgs/DashboardPage';
import CollectionsPage from './features/collections/CollectionsPage';
import EndpointsPage from './features/endpoints/EndpointsPage';

// Wrapper that blocks access to a page unless a JWT exists in localStorage.
// If no token → redirect to /login instead of rendering children.
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('vaultscale_token');
  return token ? <>{children}</> : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
        <Route path="/orgs/:orgId/collections" element={<ProtectedRoute><CollectionsPage /></ProtectedRoute>} />
        <Route path="/orgs/:orgId/collections/:collectionId/endpoints" element={<ProtectedRoute><EndpointsPage /></ProtectedRoute>} />

        {/* Default route: send visitors to dashboard, which redirects to login if not authed */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
