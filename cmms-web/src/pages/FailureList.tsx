import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { AssetReliabilityStats, FailureHistory } from '../types';

export default function FailureList() {
  const [failures, setFailures] = useState<FailureHistory[]>([]);
  const [ranking, setRanking] = useState<AssetReliabilityStats[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get<FailureHistory[]>('/failures'),
      api.get<AssetReliabilityStats[]>('/failures/reliability/ranking'),
    ]).then(([failuresRes, rankingRes]) => {
      setFailures(failuresRes.data);
      setRanking(rankingRes.data);
      setLoading(false);
    });
  }, []);

  if (loading) return <p>Carregando...</p>;

  return (
    <div>
      <h1>Falhas / MTBF / MTTR</h1>

      <h2>Confiabilidade por ativo</h2>
      <table className="data-table">
        <thead>
          <tr><th>Ativo</th><th>Falhas</th><th>MTBF (h)</th><th>MTTR (h)</th></tr>
        </thead>
        <tbody>
          {ranking.map((r) => (
            <tr key={r.assetId}>
              <td>{r.assetCode} - {r.assetName}</td>
              <td>{r.failureCount}</td>
              <td>{r.mtbfHours ?? '-'}</td>
              <td>{r.mttrHours ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Historico de falhas</h2>
      <table className="data-table">
        <thead>
          <tr><th>Ativo</th><th>Data</th><th>Classificacao</th><th>Downtime (min)</th><th>Descricao</th></tr>
        </thead>
        <tbody>
          {failures.map((f) => (
            <tr key={f.id}>
              <td>{f.assetName}</td>
              <td>{new Date(f.failedAt).toLocaleString()}</td>
              <td>{f.classification}</td>
              <td>{f.downtimeMinutes ?? '-'}</td>
              <td>{f.description ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
