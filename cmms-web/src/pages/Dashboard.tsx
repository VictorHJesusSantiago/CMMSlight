import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { AssetReliabilityStats, MaintenancePlan, Part, WorkOrder } from '../types';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

export default function Dashboard() {
  const [openOrders, setOpenOrders] = useState<WorkOrder[]>([]);
  const [overduePlans, setOverduePlans] = useState<MaintenancePlan[]>([]);
  const [lowStock, setLowStock] = useState<Part[]>([]);
  const [ranking, setRanking] = useState<AssetReliabilityStats[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      const [ordersRes, plansRes, partsRes, rankingRes] = await Promise.all([
        api.get<WorkOrder[]>('/work-orders', { params: { status: 'OPEN' } }),
        api.get<MaintenancePlan[]>('/maintenance-plans/overdue'),
        api.get<Part[]>('/parts/below-minimum'),
        api.get<AssetReliabilityStats[]>('/failures/reliability/ranking'),
      ]);
      setOpenOrders(ordersRes.data);
      setOverduePlans(plansRes.data);
      setLowStock(partsRes.data);
      setRanking(rankingRes.data.slice(0, 10));
      setLoading(false);
    }
    load();
  }, []);

  if (loading) return <p>Carregando...</p>;

  return (
    <div>
      <h1>Dashboard</h1>
      <div className="stat-cards">
        <div className="stat-card">
          <span className="stat-value">{openOrders.length}</span>
          <span className="stat-label">OS abertas</span>
        </div>
        <div className="stat-card warn">
          <span className="stat-value">{overduePlans.length}</span>
          <span className="stat-label">Planos de manutencao vencidos</span>
        </div>
        <div className="stat-card danger">
          <span className="stat-value">{lowStock.length}</span>
          <span className="stat-label">Pecas abaixo do estoque minimo</span>
        </div>
      </div>

      <h2>Ranking de falhas por ativo (Pareto)</h2>
      <ResponsiveContainer width="100%" height={320}>
        <BarChart data={ranking}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="assetCode" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="failureCount" fill="#3b6fd8" name="Falhas" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
