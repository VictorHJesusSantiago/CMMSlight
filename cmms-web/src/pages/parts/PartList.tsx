import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { Part } from '../../types';

export default function PartList() {
  const [parts, setParts] = useState<Part[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<Part[]>('/parts').then((res) => {
      setParts(res.data);
      setLoading(false);
    });
  }, []);

  if (loading) return <p>Carregando...</p>;

  return (
    <div>
      <h1>Estoque de Pecas</h1>
      <table className="data-table">
        <thead>
          <tr><th>Codigo</th><th>Nome</th><th>Qtd em estoque</th><th>Minimo</th><th>Fornecedor</th></tr>
        </thead>
        <tbody>
          {parts.map((p) => (
            <tr key={p.id} className={p.belowMinimum ? 'row-danger' : ''}>
              <td>{p.code}</td>
              <td>{p.name}</td>
              <td>{p.quantityOnHand} {p.unit}</td>
              <td>{p.minQuantity} {p.unit}</td>
              <td>{p.supplierName ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
