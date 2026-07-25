import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { AuthProvider, useAuth } from './auth/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import AssetList from './pages/assets/AssetList';
import AssetForm from './pages/assets/AssetForm';
import AssetDetail from './pages/assets/AssetDetail';
import WorkOrderList from './pages/workorders/WorkOrderList';
import WorkOrderForm from './pages/workorders/WorkOrderForm';
import WorkOrderDetail from './pages/workorders/WorkOrderDetail';
import MaintenancePlanList from './pages/maintenance/MaintenancePlanList';
import PartList from './pages/parts/PartList';
import FailureList from './pages/FailureList';
import './App.css';

function PrivateRoute({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return <p>Carregando...</p>;
  if (!user) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <PrivateRoute>
                <Layout />
              </PrivateRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="assets" element={<AssetList />} />
            <Route path="assets/new" element={<AssetForm />} />
            <Route path="assets/:id" element={<AssetDetail />} />
            <Route path="work-orders" element={<WorkOrderList />} />
            <Route path="work-orders/new" element={<WorkOrderForm />} />
            <Route path="work-orders/:id" element={<WorkOrderDetail />} />
            <Route path="maintenance-plans" element={<MaintenancePlanList />} />
            <Route path="parts" element={<PartList />} />
            <Route path="failures" element={<FailureList />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
