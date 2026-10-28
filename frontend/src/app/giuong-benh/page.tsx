'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { Badge } from '@/components/ui';
import { LABEL_GIUONG, toneForStatus } from '@/lib/format';
import type { GiuongBenh } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maGiuong', label: 'Mã giường' },
  { key: 'loaiGiuong', label: 'Loại giường' },
  {
    key: 'khoa',
    label: 'Khoa',
    render: (row) => {
      const g = row as unknown as GiuongBenh;
      return g.khoa?.tenKhoa ?? '—';
    },
  },
  {
    key: 'trangThai',
    label: 'Trạng thái',
    render: (row) => {
      const g = row as unknown as GiuongBenh;
      return <Badge text={LABEL_GIUONG[g.trangThai] ?? g.trangThai} tone={toneForStatus(g.trangThai)} />;
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
    get: (row) => (row as unknown as GiuongBenh).khoa?.id,
  },
  { name: 'maGiuong', label: 'Mã giường', type: 'text', placeholder: 'Bỏ trống để tự sinh' },
  { name: 'loaiGiuong', label: 'Loại giường', type: 'text', placeholder: 'Giường thường / ưu tiên / cấp cứu...' },
  {
    name: 'trangThai',
    label: 'Trạng thái',
    type: 'select',
    defaultValue: 'TRONG',
    options: [
      { value: 'TRONG', label: 'Trống' },
      { value: 'DANG_SU_DUNG', label: 'Đang sử dụng' },
      { value: 'BAO_TRI', label: 'Bảo trì' },
    ],
  },
];

export default function GiuongBenhPage() {
  return (
    <CrudPage
      title="Giường bệnh"
      desc="Giường bệnh thuộc khoa — mỗi lần nhập viện nằm trên một giường"
      resource="/api/giuong-benh"
      columns={columns}
      fields={fields}
      addLabel="Thêm giường"
      searchFn={(row, q) => {
        const g = row as unknown as GiuongBenh;
        return (g.maGiuong + ' ' + g.loaiGiuong + ' ' + (g.khoa?.tenKhoa ?? '')).toLowerCase().includes(q);
      }}
    />
  );
}
