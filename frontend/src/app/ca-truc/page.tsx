'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { fmtTime } from '@/lib/format';
import type { CaTruc } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maCa', label: 'Mã ca' },
  { key: 'tenCa', label: 'Tên ca' },
  {
    key: 'gioBatDau',
    label: 'Bắt đầu',
    render: (row) => fmtTime((row as unknown as CaTruc).gioBatDau?.slice(0, 5)),
  },
  {
    key: 'gioKetThuc',
    label: 'Kết thúc',
    render: (row) => fmtTime((row as unknown as CaTruc).gioKetThuc?.slice(0, 5)),
  },
];

const fields: FieldDef[] = [
  { name: 'tenCa', label: 'Tên ca', type: 'text', required: true, placeholder: 'Ca sáng / Ca chiều / Ca đêm...' },
  { name: 'maCa', label: 'Mã ca', type: 'text', placeholder: 'Bỏ trống để tự sinh' },
  { name: 'gioBatDau', label: 'Giờ bắt đầu', type: 'time' },
  { name: 'gioKetThuc', label: 'Giờ kết thúc', type: 'time' },
];

export default function CaTrucPage() {
  return (
    <CrudPage
      title="Ca trực"
      desc="Ca trực gắn với lịch làm việc của bác sĩ (quan hệ N-N trong ERD)"
      resource="/api/ca-truc"
      columns={columns}
      fields={fields}
      addLabel="Thêm ca trực"
    />
  );
}
