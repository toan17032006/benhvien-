'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { Badge, EmptyState, ErrorBox, Loading, Modal, PageHeader } from '@/components/ui';

// ===== Kiểu cấu hình cho trang CRUD generic =====

export interface FieldDef {
  /** khóa gửi lên API */
  name: string;
  label: string;
  type: 'text' | 'number' | 'date' | 'time' | 'datetime-local' | 'textarea' | 'select' | 'multiselect' | 'password' | 'email' | 'tel';
  required?: boolean;
  placeholder?: string;
  /** danh sách chọn tĩnh */
  options?: { value: string | number; label: string }[];
  /** danh sách chọn tải từ API khác */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  optionSource?: { resource: string; label: (row: any) => string };
  /** lấy giá trị ô nhập khi sửa (mặc định: row[name]) */
  get?: (row: Record<string, unknown>) => string | number | string[] | undefined | null;
  /** cột rộng gấp đôi trong form */
  full?: boolean;
  /** giá trị mặc định khi thêm mới */
  defaultValue?: string | number | string[];
}

export interface ColumnDef {
  key: string;
  label: string;
  render?: (row: Record<string, unknown>) => React.ReactNode;
  className?: string;
}

interface CrudPageProps {
  title: string;
  desc?: string;
  /** đường dẫn API, ví dụ '/api/benh-nhan' */
  resource: string;
  /** tham số lọc cố định khi tải danh sách */
  fixedParams?: Record<string, string | number | undefined | null>;
  columns: ColumnDef[];
  fields: FieldDef[];
  /** hàm tìm kiếm phía client */
  searchFn?: (row: Record<string, unknown>, q: string) => boolean;
  /** bấm vào dòng → chuyển trang */
  rowLink?: (row: Record<string, unknown>) => string;
  /** chỉnh payload trước khi gửi */
  transformSubmit?: (form: Record<string, unknown>, editing: Record<string, unknown> | null) => Record<string, unknown>;
  addLabel?: string;
  /** ẩn nút Thêm/Sửa/Xóa (trang chỉ đọc) */
  readOnly?: boolean;
}

export default function CrudPage(props: CrudPageProps) {
  const { title, desc, resource, fixedParams, columns, fields, searchFn, rowLink, transformSubmit, addLabel, readOnly } = props;

  const router = useRouter();
  const [rows, setRows] = useState<Record<string, unknown>[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [q, setQ] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<Record<string, unknown> | null>(null);
  const [form, setForm] = useState<Record<string, unknown>>({});
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [optionCache, setOptionCache] = useState<Record<string, { value: string | number; label: string }[]>>({});

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = new URLSearchParams();
      for (const [k, v] of Object.entries(fixedParams ?? {})) {
        if (v !== undefined && v !== null && v !== '') params.set(k, String(v));
      }
      const qs = params.toString();
      const data = await api.get<Record<string, unknown>[]>(`${resource}${qs ? `?${qs}` : ''}`);
      setRows(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Không tải được dữ liệu');
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resource, JSON.stringify(fixedParams ?? {})]);

  useEffect(() => {
    load();
  }, [load]);

  // Tải danh sách chọn động (optionSource)
  useEffect(() => {
    for (const f of fields) {
      if (!f.optionSource) continue;
      api
        .get<Record<string, unknown>[]>(f.optionSource.resource)
        .then((list) => {
          setOptionCache((prev) => ({
            ...prev,
            [f.name]: list.map((r) => ({ value: r.id as number, label: f.optionSource!.label(r) })),
          }));
        })
        .catch(() => {
          /* để select rỗng nếu lỗi */
        });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(fields.map((f) => f.optionSource?.resource))]);

  const openCreate = () => {
    const init: Record<string, unknown> = {};
    for (const f of fields) {
      init[f.name] = f.defaultValue !== undefined ? f.defaultValue : f.type === 'multiselect' ? [] : '';
    }
    setForm(init);
    setEditing(null);
    setFormError('');
    setModalOpen(true);
  };

  const openEdit = (row: Record<string, unknown>) => {
    const init: Record<string, unknown> = {};
    for (const f of fields) {
      const v = f.get ? f.get(row) : row[f.name];
      init[f.name] = v === undefined || v === null ? (f.type === 'multiselect' ? [] : '') : v;
    }
    setForm(init);
    setEditing(row);
    setFormError('');
    setModalOpen(true);
  };

  const submit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      const payload: Record<string, unknown> = {};
      for (const f of fields) {
        let v = form[f.name];
        if (f.type === 'multiselect') {
          v = Array.isArray(v) ? v.map(Number) : [];
        } else if (typeof v === 'string') {
          v = v.trim();
          if (v !== '' && (f.type === 'select' || f.type === 'number')) {
            v = f.type === 'number' || f.name.endsWith('Id') ? Number(v) : v;
          }
        }
        payload[f.name] = v === '' ? null : v;
      }
      const finalPayload = transformSubmit ? transformSubmit(payload, editing) : payload;
      if (editing) {
        await api.put(`${resource}/${editing.id}`, finalPayload);
      } else {
        await api.post(resource, finalPayload);
      }
      setModalOpen(false);
      await load();
    } catch (e) {
      setFormError(e instanceof Error ? e.message : 'Lưu thất bại');
    } finally {
      setSaving(false);
    }
  };

  const remove = async (row: Record<string, unknown>) => {
    if (!window.confirm('Bạn có chắc muốn xóa bản ghi này?')) return;
    try {
      await api.del(`${resource}/${row.id}`);
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Xóa thất bại');
    }
  };

  const filtered = useMemo(() => {
    if (!q.trim()) return rows;
    const needle = q.trim().toLowerCase();
    if (searchFn) return rows.filter((r) => searchFn(r, needle));
    return rows.filter((r) =>
      columns.some((c) => {
        const v = c.render ? String((c.render(r) as React.ReactElement<any>)?.props?.children ?? c.render(r)) : r[c.key];
        return String(v ?? '').toLowerCase().includes(needle);
      })
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rows, q, columns, searchFn]);

  return (
    <div>
      <PageHeader
        title={title}
        desc={desc}
        actions={
          <div className="flex items-center gap-3">
            <input
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Tìm nhanh..."
              className="w-56 rounded-lg border border-slate-300 bg-white px-3.5 py-2 text-sm outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
            />
            {!readOnly && (
              <button
                onClick={openCreate}
                className="rounded-lg bg-sky-600 px-4 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-sky-700"
              >
                + {addLabel ?? 'Thêm mới'}
              </button>
            )}
            <button
              onClick={load}
              title="Tải lại"
              className="rounded-lg border border-slate-300 bg-white p-2 text-slate-500 transition hover:bg-slate-50"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 4v5h5M20 20v-5h-5M5.07 9A8 8 0 0119 8.53M18.93 15A8 8 0 015 15.47" />
              </svg>
            </button>
          </div>
        }
      />

      {error && <ErrorBox message={error} />}

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        {loading ? (
          <Loading />
        ) : filtered.length === 0 ? (
          <EmptyState />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-slate-200 bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  {columns.map((c) => (
                    <th key={c.key} className={`px-4 py-3 font-semibold ${c.className ?? ''}`}>
                      {c.label}
                    </th>
                  ))}
                  {!readOnly && <th className="px-4 py-3 text-right font-semibold">Thao tác</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filtered.map((row, i) => (
                  <tr
                    key={String(row.id ?? i)}
                    className={`transition hover:bg-sky-50/50 ${rowLink ? 'cursor-pointer' : ''}`}
                    onClick={() => rowLink && router.push(rowLink(row))}
                  >
                    {columns.map((c) => (
                      <td key={c.key} className={`px-4 py-3 align-top ${c.className ?? ''}`}>
                        {c.render ? c.render(row) : String(row[c.key] ?? '—')}
                      </td>
                    ))}
                    {!readOnly && (
                      <td className="px-4 py-3 text-right align-top" onClick={(e) => e.stopPropagation()}>
                        <button onClick={() => openEdit(row)} className="mr-1 rounded-lg px-2.5 py-1.5 text-xs font-medium text-sky-700 transition hover:bg-sky-100">
                          Sửa
                        </button>
                        <button onClick={() => remove(row)} className="rounded-lg px-2.5 py-1.5 text-xs font-medium text-rose-600 transition hover:bg-rose-50">
                          Xóa
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal open={modalOpen} title={editing ? `Chỉnh sửa — ${title}` : `${addLabel ?? 'Thêm mới'} — ${title}`} onClose={() => setModalOpen(false)} wide={fields.length > 5}>
        <form onSubmit={submit} className="space-y-4">
          {formError && <ErrorBox message={formError} />}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {fields.map((f) => (
              <div key={f.name} className={f.full || f.type === 'textarea' ? 'sm:col-span-2' : ''}>
                <label className="mb-1.5 block text-sm font-medium text-slate-700">
                  {f.label} {f.required && <span className="text-rose-500">*</span>}
                </label>
                {f.type === 'textarea' ? (
                  <textarea
                    rows={2}
                    required={f.required}
                    value={String(form[f.name] ?? '')}
                    onChange={(e) => setForm({ ...form, [f.name]: e.target.value })}
                    placeholder={f.placeholder}
                    className="w-full rounded-lg border border-slate-300 px-3.5 py-2 text-sm outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                  />
                ) : f.type === 'select' ? (
                  <select
                    required={f.required}
                    value={String(form[f.name] ?? '')}
                    onChange={(e) => setForm({ ...form, [f.name]: e.target.value })}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2 text-sm outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                  >
                    <option value="">— Chọn {f.label.toLowerCase()} —</option>
                    {(f.options ?? optionCache[f.name] ?? []).map((o) => (
                      <option key={o.value} value={o.value}>
                        {o.label}
                      </option>
                    ))}
                  </select>
                ) : f.type === 'multiselect' ? (
                  <select
                    multiple
                    size={4}
                    value={(form[f.name] as string[]) ?? []}
                    onChange={(e) =>
                      setForm({
                        ...form,
                        [f.name]: Array.from(e.target.selectedOptions).map((o) => o.value),
                      })
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3.5 py-2 text-sm outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                  >
                    {(f.options ?? optionCache[f.name] ?? []).map((o) => (
                      <option key={o.value} value={o.value}>
                        {o.label}
                      </option>
                    ))}
                  </select>
                ) : (
                  <input
                    type={f.type}
                    required={f.required}
                    value={String(form[f.name] ?? '')}
                    onChange={(e) => setForm({ ...form, [f.name]: e.target.value })}
                    placeholder={f.placeholder}
                    className="w-full rounded-lg border border-slate-300 px-3.5 py-2 text-sm outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100"
                  />
                )}
              </div>
            ))}
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-50">
              Hủy
            </button>
            <button
              type="submit"
              disabled={saving}
              className="rounded-lg bg-sky-600 px-5 py-2 text-sm font-medium text-white shadow-sm transition hover:bg-sky-700 disabled:opacity-50"
            >
              {saving ? 'Đang lưu...' : 'Lưu'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
