import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api/client';
import type { Asset, AssetStatus, AssetType, Criticality } from '../../types';

export default function AssetForm() {
  const navigate = useNavigate();
  const [assetTypes, setAssetTypes] = useState<AssetType[]>([]);
  const [form, setForm] = useState({
    code: '',
    name: '',
    assetTypeId: '',
    location: '',
    manufacturer: '',
    model: '',
    serialNumber: '',
    status: 'ACTIVE',
    criticality: 'MEDIUM',
  });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<AssetType[]>('/asset-types').then((res) => setAssetTypes(res.data));
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const payload: Partial<Asset> & Record<string, unknown> = {
        code: form.code,
        name: form.name,
        assetTypeId: form.assetTypeId ? Number(form.assetTypeId) : undefined,
        location: form.location || undefined,
        manufacturer: form.manufacturer || undefined,
        model: form.model || undefined,
        serialNumber: form.serialNumber || undefined,
        status: form.status as AssetStatus,
        criticality: form.criticality as Criticality,
      };
      const { data } = await api.post<Asset>('/assets', payload);
      navigate(`/assets/${data.id}`);
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(message ?? 'Erro ao salvar ativo');
    }
  }

  return (
    <div>
      <h1>Novo ativo</h1>
      <form className="form" onSubmit={handleSubmit}>
        <label>Codigo
          <input required value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
        </label>
        <label>Nome
          <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </label>
        <label>Tipo de ativo
          <select value={form.assetTypeId} onChange={(e) => setForm({ ...form, assetTypeId: e.target.value })}>
            <option value="">-- nenhum --</option>
            {assetTypes.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
          </select>
        </label>
        <label>Local
          <input value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
        </label>
        <label>Fabricante
          <input value={form.manufacturer} onChange={(e) => setForm({ ...form, manufacturer: e.target.value })} />
        </label>
        <label>Modelo
          <input value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} />
        </label>
        <label>Numero de serie
          <input value={form.serialNumber} onChange={(e) => setForm({ ...form, serialNumber: e.target.value })} />
        </label>
        <label>Status
          <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            <option value="ACTIVE">Ativo</option>
            <option value="INACTIVE">Inativo</option>
            <option value="UNDER_MAINTENANCE">Em manutencao</option>
            <option value="DECOMMISSIONED">Desativado</option>
          </select>
        </label>
        <label>Criticidade
          <select value={form.criticality} onChange={(e) => setForm({ ...form, criticality: e.target.value })}>
            <option value="LOW">Baixa</option>
            <option value="MEDIUM">Media</option>
            <option value="HIGH">Alta</option>
            <option value="CRITICAL">Critica</option>
          </select>
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit">Salvar</button>
      </form>
    </div>
  );
}
