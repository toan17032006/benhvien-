'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import type { BacSi } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maBacSi', label: 'Mã BS' },
  { key: 'hoTen', label: 'Họ tên' },
  { key: 'hocHam', label: 'Học hàm' },
  { key: 'chuyenMon', label: 'Chuyên môn' },
  {
    key: 'khoa',
    label: 'Khoa',
    render: (row) => {
      const b = row as unknown as BacSi;
      return b.khoa?.tenKhoa ?? '—';
    },
  },
  { key: 'soDienThoai', label: 'SĐT' },
];

const fields: FieldDef[] = [
  {
    name: 'khoaId',
    label: 'Khoa',
    type: 'select',
    required: true,
    optionSource: { resource: '/api/khoa', label: (r) => String(r.tenKhoa ?? '') },
    get: (row) => (row as unknown as BacSi).khoa?.id,
  },
  { name: 'hoTen', label: 'Họ tên', type: 'text', required: true },
  { name: 'maBacSi', label: 'Mã bác sĩ', type: 'text', placeholder: 'Bỏ trống để tự sinh' },
  { name: 'hocHam', label: 'Học hàm / học vị', type: 'text', placeholder: 'BS / ThS / TS / GS.TS...' },
  { name: 'chuyenMon', label: 'Chuyên môn', type: 'text' },
  { name: 'soDienThoai', label: 'Số điện thoại', type: 'tel' },
  { name: 'email', label: 'Email', type: 'email' },
];

export default function BacSiPage() {
  return (
    <CrudPage
      title="Bác sĩ"
      desc="Bác sĩ thuộc khoa, trực theo lịch làm việc và thực hiện lần khám bệnh"
      resource="/api/bac-si"
      columns={columns}
      fields={fields}
      addLabel="Thêm bác sĩ"
      searchFn={(row, q) => {
        const b = row as unknown as BacSi;
        return (b.hoTen + ' ' + b.maBacSi + ' ' + (b.chuyenMon ?? '') + ' ' + (b.khoa?.tenKhoa ?? '')).toLowerCase().includes(q);
      }}
    />
  );
}
