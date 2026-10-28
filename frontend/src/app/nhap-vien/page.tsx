'use client';

import CrudPage, { type ColumnDef, type FieldDef } from '@/components/CrudPage';
import { Badge } from '@/components/ui';
import { LABEL_NHAP_VIEN, fmtDateTime, nowForInput, toneForStatus } from '@/lib/format';
import type { LanNhapVien } from '@/lib/api';

const columns: ColumnDef[] = [
  { key: 'maNhapVien', label: 'Mã nhập viện' },
  {
    key: 'ngayNhap',
    label: 'Ngày nhập',
    render: (row) => fmtDateTime((row as unknown as LanNhapVien).ngayNhap),
  },
  {
    key: 'ngayXuat',
    label: 'Ngày xuất',
    render: (row) => {
      const nv = row as unknown as LanNhapVien;
      return nv.ngayXuat ? fmtDateTime(nv.ngayXuat) : <span className="text-slate-300">—</span>;
    },
  },
  {
    key: 'giuongBenh',
    label: 'Giường',
    render: (row) => {
      const nv = row as unknown as LanNhapVien;
      return (
        <div>
          <div className="font-medium text-slate-800">{nv.giuongBenh?.maGiuong}</div>
          <div className="text-xs text-slate-500">{nv.giuongBenh?.khoa?.tenKhoa}</div>
        </div>
      );
    },
  },
  { key: 'lyDoNhapVien', label: 'Lý do' },
  {
    key: 'trangThai',
    label: 'Trạng thái',
    render: (row) => {
      const nv = row as unknown as LanNhapVien;
      return <Badge text={LABEL_NHAP_VIEN[nv.trangThai] ?? nv.trangThai} tone={toneForStatus(nv.trangThai)} />;
    },
  },
];

const fields: FieldDef[] = [
  {
    name: 'giuongBenhId',
    label: 'Giường bệnh',
    type: 'select',
    required: true,
    optionSource: {
      resource: '/api/giuong-benh',
      label: (r) => `${r.maGiuong} — ${r.loaiGiuong ?? ''} — ${r.khoa?.tenKhoa ?? ''}`,
    },
    get: (row) => (row as unknown as LanNhapVien).giuongBenh?.id,
  },
  { name: 'ngayNhap', label: 'Ngày nhập viện', type: 'datetime-local', defaultValue: nowForInput() },
  { name: 'lyDoNhapVien', label: 'Lý do nhập viện', type: 'textarea' },
  { name: 'ghiChu', label: 'Ghi chú', type: 'textarea' },
];

export default function NhapVienPage() {
  return (
    <CrudPage
      title="Lần nhập viện"
      desc="Bấm vào dòng để xem chi tiết: y tá chăm sóc, xuất viện. Khuyến nghị nhập viện từ trang Lần khám bệnh."
      resource="/api/lan-nhap-vien"
      columns={columns}
      fields={fields}
      addLabel="Thêm lần nhập viện"
      rowLink={(row) => `/nhap-vien/${row.id}`}
      searchFn={(row, q) => {
        const nv = row as unknown as LanNhapVien;
        return (nv.maNhapVien + ' ' + nv.giuongBenh?.maGiuong + ' ' + (nv.lyDoNhapVien ?? '') + ' ' + nv.giuongBenh?.khoa?.tenKhoa).toLowerCase().includes(q);
      }}
    />
  );
}
