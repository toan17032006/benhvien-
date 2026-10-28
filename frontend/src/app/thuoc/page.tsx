'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { fmtMoney } from '@/lib/format';
import type { Thuoc } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maThuoc', label: 'Mã thuốc' },
  { key: 'tenThuoc', label: 'Tên thuốc' },
  { key: 'donViTinh', label: 'Đơn vị' },
  {
    key: 'donGia',
    label: 'Đơn giá',
    render: (row) => fmtMoney((row as unknown as Thuoc).donGia),
  },
  { key: 'soLuongTon', label: 'Tồn kho' },
  { key: 'congDung', label: 'Công dụng' },
];

const fields: FieldDef[] = [
  { name: 'tenThuoc', label: 'Tên thuốc', type: 'text', required: true },
  { name: 'maThuoc', label: 'Mã thuốc', type: 'text', placeholder: 'Bỏ trống để tự sinh' },
  { name: 'donViTinh', label: 'Đơn vị tính', type: 'text', placeholder: 'Viên / Lọ / Ống...' },
  { name: 'donGia', label: 'Đơn giá (₫)', type: 'number' },
  { name: 'soLuongTon', label: 'Số lượng tồn', type: 'number' },
  { name: 'congDung', label: 'Công dụng', type: 'textarea' },
];

export default function ThuocPage() {
  return (
    <CrudPage
      title="Kho thuốc"
      desc="Thuốc được kê trong các đơn thuốc (bảng Thuốc của ERD)"
      resource="/api/thuoc"
      columns={columns}
      fields={fields}
      addLabel="Thêm thuốc"
      searchFn={(row, q) => {
        const t = row as unknown as Thuoc;
        return (t.tenThuoc + ' ' + t.maThuoc).toLowerCase().includes(q);
      }}
    />
  );
}
