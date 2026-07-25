import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client';
import type { Asset } from '../../types';

const ALERT_LABEL: Record<string, string> = {
  NONE: '',
  WATCH: 'Observar',
  ALERT: 'Alerta',
  CRITICAL_ALERT: 'Alerta critico',
};

export default function AssetList() {
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<Asset[]>('/assets').then((res) => {
      setAssets(res.data);
      setLoading(false);
    });
  }, []);

  if (loading) return <p>Carregando...</p>;

  return (
    <div>
      <div className="page-header">
        <h1>Ativos</h1>
        <Link className="button" to="/assets/new">Novo ativo</Link>
      </div>
      <table className="data-table">
        <thead>
          <tr>
            <th>Codigo</th>
            <th>Nome</th>
            <th>Tipo</th>
            <th>Local</th>
            <th>Status</th>
            <th>Criticidade</th>
            <th>Alerta</th>
          </tr>
        </thead>
        <tbody>
          {assets.map((a) => (
            <tr key={a.id}>
              <td><Link to={`/assets/${a.id}`}>{a.code}</Link></td>
              <td>{a.name}</td>
              <td>{a.assetTypeName ?? '-'}</td>
              <td>{a.location ?? '-'}</td>
              <td>{a.status}</td>
              <td>{a.criticality}</td>
              <td className={`alert-${a.criticalityAlert.toLowerCase()}`}>{ALERT_LABEL[a.criticalityAlert]}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
