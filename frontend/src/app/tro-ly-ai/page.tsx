'use client';
import { FormEvent, useState } from 'react';
import { api } from '@/lib/api';
import { useAuth, tenVaiTro } from '@/lib/auth';
type Message = { role: 'user' | 'model'; text: string };
export default function TroLyAi() {
  const { vaiTro } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  async function send(e: FormEvent) {
    e.preventDefault();
    if (busy || !input.trim()) return;
    const next: Message[] = [...messages.slice(-18), { role: 'user', text: input.trim() }];
    setBusy(true); setError('');
    try {
      const res = await api.post<{ answer: string }>('/api/ai/chat', { messages: next });
      setMessages([...next, { role: 'model', text: res.answer }]); setInput('');
    } catch (err) { setError(err instanceof Error ? err.message : 'Không thể gửi câu hỏi.'); }
    finally { setBusy(false); }
  }
  return <div className="mx-auto max-w-4xl space-y-5 p-6">
    <header className="flex justify-between gap-4"><div><h1 className="text-2xl font-bold">Trợ lý AI sức khỏe</h1><p>{vaiTro ? tenVaiTro(vaiTro) : ''}</p></div><button disabled={busy} onClick={() => { setMessages([]); setError(''); setInput(''); }} className="rounded-lg border px-4 py-2">Hội thoại mới</button></header>
    <p className="rounded-xl bg-amber-50 p-4 text-sm">AI cung cấp thông tin tham khảo, không thay thế bác sĩ. Nếu có dấu hiệu nguy hiểm, gọi 115 hoặc đến cấp cứu. Nội dung câu hỏi được gửi đến Google Gemini; tránh nhập thông tin định danh.</p>
    <section aria-label="Hội thoại" aria-live="polite" className="space-y-4">
      {!messages.length && <p className="rounded-xl bg-white p-6">Bạn muốn tìm hiểu bệnh lý hoặc triệu chứng nào? Hãy mô tả thời gian và mức độ triệu chứng.</p>}
      {messages.map((m,i) => <article key={i} className={`whitespace-pre-wrap break-words rounded-xl p-4 ${m.role === 'user' ? 'ml-8 bg-sky-100' : 'mr-8 bg-white border'}`}><strong>{m.role === 'user' ? 'Bạn' : 'Trợ lý AI'}</strong><p>{m.text}</p></article>)}
      {busy && <p role="status">AI đang trả lời…</p>}
    </section>
    {error && <p role="alert" className="text-red-700">{error} Câu hỏi được giữ lại để bạn thử lại.</p>}
    <form onSubmit={send} className="space-y-2"><label htmlFor="question" className="font-medium">Câu hỏi về sức khỏe</label><textarea id="question" value={input} disabled={busy} onChange={e=>setInput(e.target.value)} maxLength={4000} required rows={4} className="w-full rounded-xl border p-3"/><button disabled={busy || !input.trim()} className="rounded-lg bg-sky-700 px-5 py-3 text-white disabled:opacity-50">{busy ? 'Đang gửi…' : 'Gửi câu hỏi'}</button></form>
  </div>;
}
