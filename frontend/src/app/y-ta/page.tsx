'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import type { YTa } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maYTa', label: 'Mã YT' },
  { key: 'hoTen', label: 'Họ tên' },
  { key: 'trinhDo', label: 'Trình độ' },
  {
    key: 'khoa',
    label: 'Khoa',
    render: (row) => {
      const y = row as unknown as YTa;
      return y.khoa?.tenKhoa ?? '—';
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
    get: (row) => (row as unknown as YTa).khoa?.id,
  },
  { name: 'hoTen', label: 'Họ tên', type: 'text', required: true },
  { name: 'maYTa', label: 'Mã y tá', type: 'text', placeholder: 'Bỏ trống để tự sinh' },
  { name: 'trinhDo', label: 'Trình độ', type: 'text', placeholder: 'Cao đẳng / Đại học điều dưỡng...' },
  { name: 'soDienThoai', label: 'Số điện thoại', type: 'tel' },
  { name: 'email', label: 'Email', type: 'email' },
];

export default function YTaPage() {
  return (
    <CrudPage
      title="Y tá"
      desc="Y tá thuộc khoa, chăm sóc các lần nhập viện (quan hệ N-N trong ERD)"
      resource="/api/y-ta"
      columns={columns}
      fields={fields}
      addLabel="Thêm y tá"
      searchFn={(row, q) => {
        const y = row as unknown as YTa;
        return (y.hoTen + ' ' + y.maYTa + ' ' + (y.khoa?.tenKhoa ?? '')).toLowerCase().includes(q);
      }}
    />
  );
}
