'use client';

import { useEffect, useState } from 'react';
import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { api, type CaTruc, type LichCaTruc, type LichLamViec } from '@/lib/api';
import { Badge } from '@/components/ui';
import { fmtDate } from '@/lib/format';

export default function LichLamViecPage() {
  // Dữ liệu join để hiển thị ca trực của từng lịch (quan hệ N-N)
  const [lichCaTruc, setLichCaTruc] = useState<LichCaTruc[]>([]);
  const [caTrucMap, setCaTrucMap] = useState<Record<number, CaTruc>>({});
  const [ready, setReady] = useState(false);

  useEffect(() => {
    Promise.all([api.get<LichCaTruc[]>('/api/lich-ca-truc'), api.get<CaTruc[]>('/api/ca-truc')])
      .then(([lc, cs]) => {
        setLichCaTruc(lc);
        setCaTrucMap(Object.fromEntries(cs.map((c) => [c.id, c])));
      })
      .finally(() => setReady(true));
  }, []);

  if (!ready) return null;

  const caCuaLich = (lichId: number): CaTruc[] =>
    lichCaTruc.filter((x) => x.lichLamViec.id === lichId).map((x) => x.caTruc);
  const idsCaCuaLich = (lichId: number): string[] =>
    lichCaTruc.filter((x) => x.lichLamViec.id === lichId).map((x) => String(x.caTruc.id));

  const columns: ColumnDef[] = [
    { key: 'ngay', label: 'Ngày', render: (row) => fmtDate((row as unknown as LichLamViec).ngay) },
    {
      key: 'bacSi',
      label: 'Bác sĩ',
      render: (row) => {
        const l = row as unknown as LichLamViec;
        return (
          <div>
            <div className="font-medium text-slate-800">{l.bacSi?.hoTen}</div>
            <div className="text-xs text-slate-500">{l.bacSi?.chuyenMon}</div>
          </div>
        );
      },
    },
    {
      key: 'phongKham',
      label: 'Phòng khám',
      render: (row) => (row as unknown as LichLamViec).phongKham?.tenPhong ?? '—',
    },
    {
      key: 'caTruc',
      label: 'Ca trực',
      render: (row) => (
        <div className="flex flex-wrap gap-1">
          {caCuaLich((row as unknown as LichLamViec).id).map((c) => (
            <Badge key={c.id} text={c.tenCa} tone="sky" />
          ))}
        </div>
      ),
    },
    { key: 'ghiChu', label: 'Ghi chú' },
  ];

  const fields: FieldDef[] = [
    { name: 'ngay', label: 'Ngày làm việc', type: 'date', required: true },
    {
      name: 'bacSiId',
      label: 'Bác sĩ',
      type: 'select',
      required: true,
      optionSource: { resource: '/api/bac-si', label: (r) => `${r.hoTen} — ${r.chuyenMon ?? ''}` },
      get: (row) => (row as unknown as LichLamViec).bacSi?.id,
    },
    {
      name: 'phongKhamId',
      label: 'Phòng khám',
      type: 'select',
      required: true,
      optionSource: { resource: '/api/phong-kham', label: (r) => String(r.tenPhong ?? '') },
      get: (row) => (row as unknown as LichLamViec).phongKham?.id,
    },
    {
      name: 'caTrucIds',
      label: 'Ca trực (chọn nhiều — giữ Ctrl)',
      type: 'multiselect',
      optionSource: {
        resource: '/api/ca-truc',
        label: (r) => `${r.tenCa} (${(r.gioBatDau ?? '').slice(0, 5)}–${(r.gioKetThuc ?? '').slice(0, 5)})`,
      },
      get: (row) => idsCaCuaLich((row as unknown as LichLamViec).id),
    },
    { name: 'ghiChu', label: 'Ghi chú', type: 'textarea' },
  ];

  return (
    <CrudPage
      title="Lịch làm việc"
      desc="Lịch trực của bác sĩ tại phòng khám, gắn với các ca trực (N-N)"
      resource="/api/lich-lam-viec"
      columns={columns}
      fields={fields}
      addLabel="Thêm lịch"
      searchFn={(row, q) => {
        const l = row as unknown as LichLamViec;
        return (l.bacSi?.hoTen + ' ' + l.phongKham?.tenPhong + ' ' + caCuaLich(l.id).map((c) => c.tenCa).join(' ')).toLowerCase().includes(q);
      }}
    />
  );
}
