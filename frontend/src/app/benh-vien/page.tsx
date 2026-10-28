'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';

const columns: ColumnDef[] = [
  { key: 'ten', label: 'Tên bệnh viện' },
  { key: 'diaChi', label: 'Địa chỉ' },
  { key: 'soDienThoai', label: 'Số điện thoại' },
  { key: 'email', label: 'Email' },
];

const fields: FieldDef[] = [
  { name: 'ten', label: 'Tên bệnh viện', type: 'text', required: true, full: true },
  { name: 'diaChi', label: 'Địa chỉ', type: 'text', full: true },
  { name: 'soDienThoai', label: 'Số điện thoại', type: 'tel' },
  { name: 'email', label: 'Email', type: 'email' },
];

export default function BenhVienPage() {
  return (
    <CrudPage
      title="Bệnh viện"
      desc="Thông tin các bệnh viện trong hệ thống (thực thể gốc của sơ đồ ERD)"
      resource="/api/benh-vien"
      columns={columns}
      fields={fields}
      addLabel="Thêm bệnh viện"
    />
  );
}
