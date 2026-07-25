import { useEffect, useState, type FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../../api/client';
import type { WorkOrder, WorkOrderEvent, WorkOrderStatus } from '../../types';

interface ChecklistResult {
  id: number;
  checklistItemId: number;
  itemDescription: string;
  completed: boolean;
  value?: string;
  notes?: string;
}

interface WorkOrderPart {
  id: number;
  partId: number;
  partName: string;
  quantityUsed: number;
}

const NEXT_STATUS: Record<WorkOrderStatus, WorkOrderStatus[]> = {
  OPEN: ['SCHEDULED', 'IN_PROGRESS', 'CANCELLED'],
  SCHEDULED: ['IN_PROGRESS', 'OPEN', 'CANCELLED'],
  IN_PROGRESS: ['DONE', 'CANCELLED'],
  DONE: [],
  CANCELLED: [],
};

export default function WorkOrderDetail() {
  const { id } = useParams();
  const [wo, setWo] = useState<WorkOrder | null>(null);
  const [timeline, setTimeline] = useState<WorkOrderEvent[]>([]);
  const [checklist, setChecklist] = useState<ChecklistResult[]>([]);
  const [parts, setParts] = useState<WorkOrderPart[]>([]);
  const [comment, setComment] = useState('');
  const [signName, setSignName] = useState('');

  async function loadAll() {
    const [woRes, timelineRes, checklistRes, partsRes] = await Promise.all([
      api.get<WorkOrder>(`/work-orders/${id}`),
      api.get<WorkOrderEvent[]>(`/work-orders/${id}/timeline`),
      api.get<ChecklistResult[]>(`/work-orders/${id}/checklist`),
      api.get<WorkOrderPart[]>(`/work-orders/${id}/parts`),
    ]);
    setWo(woRes.data);
    setTimeline(timelineRes.data);
    setChecklist(checklistRes.data);
    setParts(partsRes.data);
  }

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function changeStatus(newStatus: WorkOrderStatus) {
    await api.post(`/work-orders/${id}/status`, { newStatus });
    loadAll();
  }

  async function handleComment(e: FormEvent) {
    e.preventDefault();
    if (!comment.trim()) return;
    await api.post(`/work-orders/${id}/comments`, { message: comment });
    setComment('');
    loadAll();
  }

  async function handleSign(e: FormEvent) {
    e.preventDefault();
    await api.post(`/work-orders/${id}/sign`, { signedByName: signName });
    setSignName('');
    loadAll();
  }

  async function handleReopen() {
    const reason = window.prompt('Motivo do retrabalho:');
    if (reason === null) return;
    const { data } = await api.post<WorkOrder>(`/work-orders/${id}/reopen`, { reason });
    window.location.href = `/work-orders/${data.id}`;
  }

  async function answerChecklistItem(checklistItemId: number, value: string) {
    await api.post(`/work-orders/${id}/checklist/answers`, { checklistItemId, value });
    loadAll();
  }

  if (!wo) return <p>Carregando...</p>;

  return (
    <div>
      <div className="page-header">
        <h1>{wo.code} <span className="code-badge">{wo.status}</span></h1>
        <a className="button" href={`/api/work-orders/${id}/pdf`} target="_blank" rel="noreferrer">Exportar PDF</a>
      </div>

      <div className="detail-grid">
        <div><strong>Titulo:</strong> {wo.title}</div>
        <div><strong>Ativo:</strong> {wo.assetName}</div>
        <div><strong>Tipo:</strong> {wo.type}</div>
        <div><strong>Prioridade:</strong> {wo.priority}</div>
        <div><strong>Tecnico:</strong> {wo.assignedToName ?? '-'}</div>
        <div><strong>Tempo de execucao:</strong> {wo.executionMinutes != null ? `${wo.executionMinutes} min` : '-'}</div>
      </div>

      <section>
        <h2>Mudar status</h2>
        <div className="button-row">
          {NEXT_STATUS[wo.status].map((s) => (
            <button key={s} onClick={() => changeStatus(s)}>{s}</button>
          ))}
          {NEXT_STATUS[wo.status].length === 0 && <span>Nenhuma transicao disponivel</span>}
        </div>
        {(wo.status === 'DONE' || wo.status === 'CANCELLED') && (
          <button onClick={handleReopen}>Reabrir (retrabalho)</button>
        )}
      </section>

      {wo.status === 'DONE' && !wo.signedByName && (
        <section>
          <h2>Assinatura</h2>
          <form onSubmit={handleSign} className="inline-form">
            <input placeholder="Nome do responsavel" value={signName} onChange={(e) => setSignName(e.target.value)} required />
            <button type="submit">Assinar</button>
          </form>
        </section>
      )}
      {wo.signedByName && <p><strong>Assinado por:</strong> {wo.signedByName} em {wo.signedAt && new Date(wo.signedAt).toLocaleString()}</p>}

      <section>
        <h2>Checklist</h2>
        {checklist.length === 0 && <p>Nenhum checklist vinculado a esta OS.</p>}
        <ul className="checklist">
          {checklist.map((c) => (
            <li key={c.id}>
              <label>
                <input type="checkbox" checked={c.value === 'true'} onChange={(e) => answerChecklistItem(c.checklistItemId, String(e.target.checked))} />
                {c.itemDescription}
              </label>
            </li>
          ))}
        </ul>
      </section>

      <section>
        <h2>Pecas utilizadas</h2>
        <ul>
          {parts.map((p) => <li key={p.id}>{p.partName} - {p.quantityUsed}</li>)}
        </ul>
      </section>

      <section>
        <h2>Historico / Timeline</h2>
        <form onSubmit={handleComment} className="inline-form">
          <input placeholder="Adicionar comentario" value={comment} onChange={(e) => setComment(e.target.value)} />
          <button type="submit">Comentar</button>
        </form>
        <ul className="timeline">
          {timeline.map((ev) => (
            <li key={ev.id}>
              <small>{new Date(ev.createdAt).toLocaleString()}</small>
              <strong> [{ev.eventType}]</strong> {ev.message} {ev.createdByUserName && `- ${ev.createdByUserName}`}
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
