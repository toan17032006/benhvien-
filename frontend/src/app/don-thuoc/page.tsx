'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { fmtDateTime, nowForInput } from '@/lib/format';
import type { DonThuoc } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maDonThuoc', label: 'Mã đơn' },
  {
    key: 'ngayKe',
    label: 'Ngày kê',
    render: (row) => fmtDateTime((row as unknown as DonThuoc).ngayKe),
  },
  {
    key: 'benhNhan',
    label: 'Bệnh nhân',
    render: (row) => {
      const d = row as unknown as DonThuoc;
      return (
        <div>
          <div className="font-medium text-slate-800">{d.lanKhamBenh?.benhNhan?.hoTen}</div>
          <div className="text-xs text-slate-500">{d.lanKhamBenh?.benhNhan?.maBenhNhan}</div>
        </div>
      );
    },
  },
  {
    key: 'lanKham',
    label: 'Lần khám',
    render: (row) => (row as unknown as DonThuoc).lanKhamBenh?.maLanKham ?? '—',
  },
  { key: 'ghiChu', label: 'Ghi chú' },
];

const fields: FieldDef[] = [
  {
    name: 'lanKhamBenhId',
    label: 'Lần khám bệnh',
    type: 'select',
    required: true,
    optionSource: {
      resource: '/api/lan-kham-benh',
      label: (r) => `${r.maLanKham} — ${r.benhNhan?.hoTen ?? ''}`,
    },
    get: (row) => (row as unknown as DonThuoc).lanKhamBenh?.id,
  },
  { name: 'ngayKe', label: 'Ngày kê', type: 'datetime-local', defaultValue: nowForInput() },
  { name: 'ghiChu', label: 'Ghi chú', type: 'textarea' },
];

export default function DonThuocPage() {
  return (
    <CrudPage
      title="Đơn thuốc"
      desc="Mỗi lần khám có thể có nhiều đơn thuốc; chi tiết thuốc được quản lý trong trang chi tiết lần khám"
      resource="/api/don-thuoc"
      columns={columns}
      fields={fields}
      addLabel="Kê đơn"
      rowLink={(row) => `/kham-benh/${(row as unknown as DonThuoc).lanKhamBenh?.id}`}
      searchFn={(row, q) => {
        const d = row as unknown as DonThuoc;
        return (d.maDonThuoc + ' ' + d.lanKhamBenh?.benhNhan?.hoTen + ' ' + d.lanKhamBenh?.maLanKham).toLowerCase().includes(q);
      }}
    />
  );
}
