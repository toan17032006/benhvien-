// ===== Định dạng ngày tháng, tiền tệ và nhãn tiếng Việt =====

export function fmtDate(iso?: string | null): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export function fmtDateTime(iso?: string | null): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  return d.toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

export function fmtTime(iso?: string | null): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso.slice(0, 5);
  return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

export function fmtMoney(n?: number | null): string {
  if (n === undefined || n === null) return '—';
  return n.toLocaleString('vi-VN') + ' ₫';
}

// Giá trị cho input datetime-local: "2026-10-26T14:30"
export function nowForInput(): string {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

export function todayForInput(): string {
  return new Date().toISOString().slice(0, 10);
}

export const LABEL_GIUONG: Record<string, string> = {
  TRONG: 'Trống',
  DANG_SU_DUNG: 'Đang sử dụng',
  BAO_TRI: 'Bảo trì',
};

export const LABEL_KHAM: Record<string, string> = {
  DANG_KHAM: 'Đang khám',
  HOAN_THANH: 'Hoàn thành',
};

export const LABEL_NHAP_VIEN: Record<string, string> = {
  DANG_DIEU_TRI: 'Đang điều trị',
  DA_XUAT_VIEN: 'Đã xuất viện',
};

export const LABEL_XET_NGHIEM: Record<string, string> = {
  CHO_KET_QUA: 'Chờ kết quả',
  CO_KET_QUA: 'Có kết quả',
};

/** Màu badge cho từng trạng thái. */
export function toneForStatus(status: string): 'green' | 'yellow' | 'red' | 'gray' | 'sky' {
  switch (status) {
    case 'TRONG':
    case 'HOAN_THANH':
    case 'CO_KET_QUA':
      return 'green';
    case 'DANG_KHAM':
    case 'CHO_KET_QUA':
    case 'DANG_DIEU_TRI':
      return 'yellow';
    case 'DANG_SU_DUNG':
      return 'sky';
    case 'BAO_TRI':
    case 'DA_XUAT_VIEN':
      return 'gray';
    default:
      return 'gray';
  }
}
