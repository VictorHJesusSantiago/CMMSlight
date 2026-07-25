import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client';
import type { WorkOrder, WorkOrderStatus } from '../../types';

const COLUMNS: { status: WorkOrderStatus; label: string }[] = [
  { status: 'OPEN', label: 'Aberta' },
  { status: 'SCHEDULED', label: 'Agendada' },
  { status: 'IN_PROGRESS', label: 'Em andamento' },
  { status: 'DONE', label: 'Concluida' },
  { status: 'CANCELLED', label: 'Cancelada' },
];

export default function WorkOrderList() {
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<WorkOrder[]>('/work-orders').then((res) => {
      setOrders(res.data);
      setLoading(false);
    });
  }, []);

  if (loading) return <p>Carregando...</p>;

  return (
    <div>
      <div className="page-header">
        <h1>Ordens de Servico</h1>
        <Link className="button" to="/work-orders/new">Nova OS</Link>
      </div>
      <div className="kanban">
        {COLUMNS.map((col) => (
          <div key={col.status} className="kanban-column">
            <h3>{col.label} ({orders.filter((o) => o.status === col.status).length})</h3>
            {orders.filter((o) => o.status === col.status).map((o) => (
              <Link key={o.id} to={`/work-orders/${o.id}`} className={`kanban-card priority-${o.priority.toLowerCase()}`}>
                <strong>{o.code}</strong>
                <span>{o.title}</span>
                <small>{o.assetName}</small>
              </Link>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}
