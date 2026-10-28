'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { Badge } from '@/components/ui';
import { LABEL_XET_NGHIEM, fmtDateTime, nowForInput, toneForStatus } from '@/lib/format';
import type { XetNghiem } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maXetNghiem', label: 'Mã XN' },
  { key: 'tenXetNghiem', label: 'Tên xét nghiệm' },
  { key: 'loaiXetNghiem', label: 'Loại' },
  {
    key: 'thoiGian',
    label: 'Thời gian',
    render: (row) => fmtDateTime((row as unknown as XetNghiem).thoiGian),
  },
  {
    key: 'benhNhan',
    label: 'Bệnh nhân',
    render: (row) => {
      const x = row as unknown as XetNghiem;
      return (
        <div>
          <div className="font-medium text-slate-800">{x.lanKhamBenh?.benhNhan?.hoTen}</div>
          <div className="text-xs text-slate-500">{x.lanKhamBenh?.maLanKham}</div>
        </div>
      );
    },
  },
  { key: 'ketQua', label: 'Kết quả' },
  {
    key: 'trangThai',
    label: 'Trạng thái',
    render: (row) => {
      const x = row as unknown as XetNghiem;
      return <Badge text={LABEL_XET_NGHIEM[x.trangThai] ?? x.trangThai} tone={toneForStatus(x.trangThai)} />;
    },
  },
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
    get: (row) => (row as unknown as XetNghiem).lanKhamBenh?.id,
  },
  { name: 'tenXetNghiem', label: 'Tên xét nghiệm', type: 'text', required: true, placeholder: 'Công thức máu, X-quang...' },
  { name: 'loaiXetNghiem', label: 'Loại xét nghiệm', type: 'text', placeholder: 'Máu / Nước tiểu / Cận lâm sàng...' },
  {
    name: 'lichLamViecId',
    label: 'Lịch thực hiện (kỹ thuật viên)',
    type: 'select',
    optionSource: {
      resource: '/api/lich-lam-viec',
      label: (r) => `${(r.ngay ?? '').split('-').reverse().join('/')} — ${r.bacSi?.hoTen ?? ''}`,
    },
    get: (row) => (row as unknown as XetNghiem).lichLamViec?.id,
  },
  { name: 'thoiGian', label: 'Thời gian', type: 'datetime-local', defaultValue: nowForInput() },
  {
    name: 'trangThai',
    label: 'Trạng thái',
    type: 'select',
    defaultValue: 'CHO_KET_QUA',
    options: [
      { value: 'CHO_KET_QUA', label: 'Chờ kết quả' },
      { value: 'CO_KET_QUA', label: 'Có kết quả' },
    ],
    get: (row) => (row as unknown as XetNghiem).trangThai,
  },
  { name: 'ketQua', label: 'Kết quả', type: 'textarea' },
];

export default function XetNghiemPage() {
  return (
    <CrudPage
      title="Xét nghiệm"
      desc="Xét nghiệm thuộc lần khám bệnh, được thực hiện trên một lịch làm việc"
      resource="/api/xet-nghiem"
      columns={columns}
      fields={fields}
      addLabel="Thêm xét nghiệm"
      rowLink={(row) => `/kham-benh/${(row as unknown as XetNghiem).lanKhamBenh?.id}`}
      searchFn={(row, q) => {
        const x = row as unknown as XetNghiem;
        return (x.maXetNghiem + ' ' + x.tenXetNghiem + ' ' + x.lanKhamBenh?.benhNhan?.hoTen).toLowerCase().includes(q);
      }}
    />
  );
}
