'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { fmtDate } from '@/lib/format';
import type { BenhNhan } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maBenhNhan', label: 'Mã BN' },
  { key: 'hoTen', label: 'Họ tên' },
  {
    key: 'ngaySinh',
    label: 'Ngày sinh',
    render: (row) => fmtDate((row as unknown as BenhNhan).ngaySinh),
  },
  { key: 'gioiTinh', label: 'Giới tính' },
  { key: 'soDienThoai', label: 'SĐT' },
  { key: 'diaChi', label: 'Địa chỉ' },
];

const fields: FieldDef[] = [
  { name: 'hoTen', label: 'Họ tên', type: 'text', required: true },
  { name: 'maBenhNhan', label: 'Mã bệnh nhân', type: 'text', placeholder: 'Bỏ trống để tự sinh' },
  { name: 'ngaySinh', label: 'Ngày sinh', type: 'date' },
  {
    name: 'gioiTinh',
    label: 'Giới tính',
    type: 'select',
    options: [
      { value: 'Nam', label: 'Nam' },
      { value: 'Nữ', label: 'Nữ' },
      { value: 'Khác', label: 'Khác' },
    ],
  },
  { name: 'soDienThoai', label: 'Số điện thoại', type: 'tel' },
  { name: 'diaChi', label: 'Địa chỉ', type: 'text', full: true },
  { name: 'tienSuBenhAn', label: 'Tiền sử bệnh án', type: 'textarea' },
];

export default function BenhNhanPage() {
  return (
    <CrudPage
      title="Bệnh nhân"
      desc="Một bệnh nhân có nhiều lần khám bệnh (quan hệ 1-N trong ERD)"
      resource="/api/benh-nhan"
      columns={columns}
      fields={fields}
      addLabel="Thêm bệnh nhân"
      searchFn={(row, q) => {
        const b = row as unknown as BenhNhan;
        return (b.hoTen + ' ' + b.maBenhNhan + ' ' + (b.soDienThoai ?? '')).toLowerCase().includes(q);
      }}
    />
  );
}
