import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { MaintenancePlan } from '../../types';

export default function MaintenancePlanList() {
  const [plans, setPlans] = useState<MaintenancePlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);

  async function load() {
    const res = await api.get<MaintenancePlan[]>('/maintenance-plans');
    setPlans(res.data);
    setLoading(false);
  }

  useEffect(() => {
    load();
  }, []);

  async function handleGenerateDue() {
    setGenerating(true);
    await api.post('/maintenance-plans/generate-due');
    await load();
    setGenerating(false);
  }

  if (loading) return <p>Carregando...</p>;

  return (
    <div>
      <div className="page-header">
        <h1>Planos de Manutencao Preventiva</h1>
        <button onClick={handleGenerateDue} disabled={generating}>
          {generating ? 'Gerando...' : 'Gerar OS vencidas agora'}
        </button>
      </div>
      <table className="data-table">
        <thead>
          <tr>
            <th>Nome</th><th>Ativo/Tipo</th><th>Frequencia</th><th>Proximo vencimento</th><th>Status</th>
          </tr>
        </thead>
        <tbody>
          {plans.map((p) => (
            <tr key={p.id} className={p.overdue ? 'row-danger' : ''}>
              <td>{p.name}</td>
              <td>{p.assetName ?? p.assetTypeName ?? '-'}</td>
              <td>{p.frequencyValue} {p.frequencyUnit ?? 'DAYS'}</td>
              <td>{p.nextDueAt ? new Date(p.nextDueAt).toLocaleDateString() : '-'}</td>
              <td>{p.overdue ? 'Vencido' : p.active ? 'Em dia' : 'Inativo'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
