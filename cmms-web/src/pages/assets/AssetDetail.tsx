import { useEffect, useState, type FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../../api/client';
import type { Asset } from '../../types';

interface Attachment {
  id: number;
  fileName: string;
  category: string;
  sizeBytes: number;
  downloadUrl: string;
}

interface LocationHistoryEntry {
  id: number;
  previousLocation?: string;
  newLocation: string;
  movedAt: string;
  movedByUserName?: string;
  notes?: string;
}

type Tab = 'info' | 'attachments' | 'qrcode' | 'history';

export default function AssetDetail() {
  const { id } = useParams();
  const [asset, setAsset] = useState<Asset | null>(null);
  const [tab, setTab] = useState<Tab>('info');
  const [attachments, setAttachments] = useState<Attachment[]>([]);
  const [history, setHistory] = useState<LocationHistoryEntry[]>([]);
  const [newLocation, setNewLocation] = useState('');

  async function loadAll() {
    const [assetRes, attachRes, historyRes] = await Promise.all([
      api.get<Asset>(`/assets/${id}`),
      api.get<Attachment[]>(`/assets/${id}/attachments`),
      api.get<LocationHistoryEntry[]>(`/assets/${id}/location-history`),
    ]);
    setAsset(assetRes.data);
    setAttachments(attachRes.data);
    setHistory(historyRes.data);
  }

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function handleUpload(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fileInput = e.currentTarget.elements.namedItem('file') as HTMLInputElement;
    if (!fileInput.files || fileInput.files.length === 0) return;
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    await api.post(`/assets/${id}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    fileInput.value = '';
    loadAll();
  }

  async function handleMove(e: FormEvent) {
    e.preventDefault();
    await api.post(`/assets/${id}/move`, { newLocation });
    setNewLocation('');
    loadAll();
  }

  if (!asset) return <p>Carregando...</p>;

  return (
    <div>
      <div className="page-header">
        <h1>{asset.name} <span className="code-badge">{asset.code}</span></h1>
      </div>

      <div className="tabs">
        <button className={tab === 'info' ? 'active' : ''} onClick={() => setTab('info')}>Informacoes</button>
        <button className={tab === 'attachments' ? 'active' : ''} onClick={() => setTab('attachments')}>Anexos</button>
        <button className={tab === 'qrcode' ? 'active' : ''} onClick={() => setTab('qrcode')}>QR Code</button>
        <button className={tab === 'history' ? 'active' : ''} onClick={() => setTab('history')}>Historico de local</button>
      </div>

      {tab === 'info' && (
        <div className="detail-grid">
          <div><strong>Status:</strong> {asset.status}</div>
          <div><strong>Criticidade:</strong> {asset.criticality} ({asset.criticalityAlert})</div>
          <div><strong>Local atual:</strong> {asset.location ?? '-'}</div>
          <div><strong>Fabricante:</strong> {asset.manufacturer ?? '-'}</div>
          <div><strong>Modelo:</strong> {asset.model ?? '-'}</div>
          <div><strong>Numero de serie:</strong> {asset.serialNumber ?? '-'}</div>
          <div><strong>Garantia ate:</strong> {asset.warrantyExpiration ?? '-'} {asset.warrantyExpired && '(vencida)'}</div>
          <div><strong>Valor depreciado atual:</strong> {asset.currentDepreciatedValue ?? '-'}</div>
        </div>
      )}

      {tab === 'attachments' && (
        <div>
          <form onSubmit={handleUpload} className="inline-form">
            <input type="file" name="file" required />
            <button type="submit">Enviar anexo</button>
          </form>
          <ul className="attachment-list">
            {attachments.map((a) => (
              <li key={a.id}>
                <a href={a.downloadUrl} target="_blank" rel="noreferrer">{a.fileName}</a> ({a.category}, {(a.sizeBytes / 1024).toFixed(1)} KB)
              </li>
            ))}
          </ul>
        </div>
      )}

      {tab === 'qrcode' && (
        <div>
          <img src={`/api/assets/${id}/qrcode`} alt="QR Code do ativo" width={250} height={250} />
          <p>Escaneie para identificar o ativo {asset.code} rapidamente no chao de fabrica.</p>
        </div>
      )}

      {tab === 'history' && (
        <div>
          <form onSubmit={handleMove} className="inline-form">
            <input placeholder="Novo local" value={newLocation} onChange={(e) => setNewLocation(e.target.value)} required />
            <button type="submit">Registrar movimentacao</button>
          </form>
          <table className="data-table">
            <thead>
              <tr><th>Data</th><th>De</th><th>Para</th><th>Por</th><th>Notas</th></tr>
            </thead>
            <tbody>
              {history.map((h) => (
                <tr key={h.id}>
                  <td>{new Date(h.movedAt).toLocaleString()}</td>
                  <td>{h.previousLocation ?? '-'}</td>
                  <td>{h.newLocation}</td>
                  <td>{h.movedByUserName ?? '-'}</td>
                  <td>{h.notes ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
