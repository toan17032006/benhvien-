'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { Badge } from '@/components/ui';
import { LABEL_KHAM, fmtDateTime, nowForInput, toneForStatus } from '@/lib/format';
import type { LanKhamBenh } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maLanKham', label: 'Mã lần khám' },
  {
    key: 'benhNhan',
    label: 'Bệnh nhân',
    render: (row) => {
      const k = row as unknown as LanKhamBenh;
      return (
        <div>
          <div className="font-medium text-slate-800">{k.benhNhan?.hoTen}</div>
          <div className="text-xs text-slate-500">{k.benhNhan?.maBenhNhan}</div>
        </div>
      );
    },
  },
  {
    key: 'thoiGianKham',
    label: 'Thời gian',
    render: (row) => fmtDateTime((row as unknown as LanKhamBenh).thoiGianKham),
  },
  {
    key: 'bacSi',
    label: 'Bác sĩ',
    render: (row) => (row as unknown as LanKhamBenh).lichLamViec?.bacSi?.hoTen ?? '—',
  },
  { key: 'chanDoan', label: 'Chẩn đoán' },
  {
    key: 'trangThai',
    label: 'Trạng thái',
    render: (row) => {
      const k = row as unknown as LanKhamBenh;
      return <Badge text={LABEL_KHAM[k.trangThai] ?? k.trangThai} tone={toneForStatus(k.trangThai)} />;
    },
  },
  {
    key: 'lanNhapVien',
    label: 'Nhập viện',
    render: (row) => {
      const k = row as unknown as LanKhamBenh;
      return k.lanNhapVien ? <Badge text={`✓ ${k.lanNhapVien.maNhapVien}`} tone="red" /> : <span className="text-slate-300">—</span>;
    },
  },
];

const fields: FieldDef[] = [
  {
    name: 'benhNhanId',
    label: 'Bệnh nhân',
    type: 'select',
    required: true,
    optionSource: { resource: '/api/benh-nhan', label: (r) => `${r.hoTen} — ${r.maBenhNhan}` },
    get: (row) => (row as unknown as LanKhamBenh).benhNhan?.id,
  },
  {
    name: 'lichLamViecId',
    label: 'Lịch làm việc (bác sĩ trực)',
    type: 'select',
    optionSource: {
      resource: '/api/lich-lam-viec',
      label: (r) => `${(r.ngay ?? '').split('-').reverse().join('/')} — ${r.bacSi?.hoTen ?? ''} — ${r.phongKham?.tenPhong ?? ''}`,
    },
    get: (row) => (row as unknown as LanKhamBenh).lichLamViec?.id,
  },
  { name: 'thoiGianKham', label: 'Thời gian khám', type: 'datetime-local', defaultValue: nowForInput() },
  {
    name: 'trangThai',
    label: 'Trạng thái',
    type: 'select',
    defaultValue: 'DANG_KHAM',
    options: [
      { value: 'DANG_KHAM', label: 'Đang khám' },
      { value: 'HOAN_THANH', label: 'Hoàn thành' },
    ],
    get: (row) => (row as unknown as LanKhamBenh).trangThai,
  },
  { name: 'trieuChung', label: 'Triệu chứng', type: 'textarea' },
  { name: 'chanDoan', label: 'Chẩn đoán', type: 'textarea' },
];

export default function KhamBenhPage() {
  return (
    <CrudPage
      title="Lần khám bệnh"
      desc="Bấm vào dòng để xem chi tiết — kê đơn thuốc, xét nghiệm, nhập viện"
      resource="/api/lan-kham-benh"
      columns={columns}
      fields={fields}
      addLabel="Đăng ký khám"
      rowLink={(row) => `/kham-benh/${row.id}`}
      searchFn={(row, q) => {
        const k = row as unknown as LanKhamBenh;
        return (k.maLanKham + ' ' + k.benhNhan?.hoTen + ' ' + (k.chanDoan ?? '') + ' ' + (k.trieuChung ?? '')).toLowerCase().includes(q);
      }}
    />
  );
}
