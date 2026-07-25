import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/client';
import type { Asset, WorkOrder } from '../../types';

export default function WorkOrderForm() {
  const navigate = useNavigate();
  const [assets, setAssets] = useState<Asset[]>([]);
  const [form, setForm] = useState({
    code: '',
    assetId: '',
    type: 'CORRECTIVE',
    priority: 'MEDIUM',
    title: '',
    description: '',
  });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<Asset[]>('/assets').then((res) => setAssets(res.data));
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const { data } = await api.post<WorkOrder>('/work-orders', {
        code: form.code,
        assetId: Number(form.assetId),
        type: form.type,
        priority: form.priority,
        title: form.title,
        description: form.description || undefined,
      });
      navigate(`/work-orders/${data.id}`);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(message ?? 'Erro ao criar OS');
    }
  }

  return (
    <div>
      <h1>Nova Ordem de Servico</h1>
      <form className="form" onSubmit={handleSubmit}>
        <label>Codigo
          <input required value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
        </label>
        <label>Ativo
          <select required value={form.assetId} onChange={(e) => setForm({ ...form, assetId: e.target.value })}>
            <option value="">-- selecione --</option>
            {assets.map((a) => <option key={a.id} value={a.id}>{a.code} - {a.name}</option>)}
          </select>
        </label>
        <label>Tipo
          <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            <option value="CORRECTIVE">Corretiva</option>
            <option value="PREVENTIVE">Preventiva</option>
            <option value="PREDICTIVE">Preditiva</option>
          </select>
        </label>
        <label>Prioridade
          <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
            <option value="LOW">Baixa</option>
            <option value="MEDIUM">Media</option>
            <option value="HIGH">Alta</option>
            <option value="URGENT">Urgente</option>
          </select>
        </label>
        <label>Titulo
          <input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </label>
        <label>Descricao
          <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit">Criar OS</button>
      </form>
    </div>
  );
}
