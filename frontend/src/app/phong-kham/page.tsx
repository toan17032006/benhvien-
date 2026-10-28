'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import type { PhongKham } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maPhong', label: 'Mã phòng' },
  { key: 'tenPhong', label: 'Tên phòng' },
  {
    key: 'khoa',
    label: 'Khoa',
    render: (row) => {
      const p = row as unknown as PhongKham;
      return p.khoa?.tenKhoa ?? '—';
    },
  },
];

const fields: FieldDef[] = [
  {
    name: 'khoaId',
    label: 'Khoa',
    type: 'select',
    required: true,
    optionSource: { resource: '/api/khoa', label: (r) => String(r.tenKhoa ?? '') },
    get: (row) => (row as unknown as PhongKham).khoa?.id,
  },
  { name: 'maPhong', label: 'Mã phòng', type: 'text', placeholder: 'Bỏ trống để tự sinh' },
  { name: 'tenPhong', label: 'Tên phòng', type: 'text', required: true },
];

export default function PhongKhamPage() {
  return (
    <CrudPage
      title="Phòng khám"
      desc="Phòng khám thuộc khoa — nơi bác sĩ trực theo lịch làm việc"
      resource="/api/phong-kham"
      columns={columns}
      fields={fields}
      addLabel="Thêm phòng khám"
      searchFn={(row, q) => {
        const p = row as unknown as PhongKham;
        return (p.tenPhong + ' ' + p.maPhong + ' ' + (p.khoa?.tenKhoa ?? '')).toLowerCase().includes(q);
      }}
    />
  );
}
