'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import type { Khoa } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maKhoa', label: 'Mã khoa' },
  { key: 'tenKhoa', label: 'Tên khoa' },
  {
    key: 'benhVien',
    label: 'Bệnh viện',
    render: (row) => {
      const k = row as unknown as Khoa;
      return k.benhVien?.ten ?? '—';
    },
  },
  { key: 'moTa', label: 'Mô tả' },
];

const fields: FieldDef[] = [
  {
    name: 'benhVienId',
    label: 'Bệnh viện',
    type: 'select',
    required: true,
    optionSource: { resource: '/api/benh-vien', label: (r) => String(r.ten ?? '') },
    get: (row) => (row as unknown as Khoa).benhVien?.id,
  },
  { name: 'maKhoa', label: 'Mã khoa', type: 'text', placeholder: 'Bỏ trống để tự sinh' },
  { name: 'tenKhoa', label: 'Tên khoa', type: 'text', required: true },
  { name: 'moTa', label: 'Mô tả', type: 'textarea' },
];

export default function KhoaPage() {
  return (
    <CrudPage
      title="Khoa"
      desc="Mỗi bệnh viện có nhiều khoa — khoa quản lý giường bệnh, phòng khám, bác sĩ, y tá"
      resource="/api/khoa"
      columns={columns}
      fields={fields}
      addLabel="Thêm khoa"
      searchFn={(row, q) => {
        const k = row as unknown as Khoa;
        return (k.tenKhoa + ' ' + k.maKhoa).toLowerCase().includes(q);
      }}
    />
  );
}
