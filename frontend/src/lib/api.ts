// ===== API client gọi Spring Boot qua proxy /api của Next.js =====

export interface BenhVien {
  id: number;
  ten: string;
  diaChi?: string | null;
  soDienThoai?: string | null;
  email?: string | null;
  viDo?: number | null;
  kinhDo?: number | null;
}

export interface Khoa {
  id: number;
  maKhoa: string;
  tenKhoa: string;
  moTa?: string | null;
  benhVien: BenhVien;
}

export interface GiuongBenh {
  id: number;
  maGiuong: string;
  loaiGiuong?: string | null;
  trangThai: 'TRONG' | 'DANG_SU_DUNG' | 'BAO_TRI';
  khoa: Khoa;
}

export interface PhongKham {
  id: number;
  maPhong: string;
  tenPhong: string;
  khoa: Khoa;
}

export interface BacSi {
  id: number;
  maBacSi: string;
  hoTen: string;
  chuyenMon?: string | null;
  hocHam?: string | null;
  soDienThoai?: string | null;
  email?: string | null;
  khoa: Khoa;
}

export interface YTa {
  id: number;
  maYTa: string;
  hoTen: string;
  trinhDo?: string | null;
  soDienThoai?: string | null;
  email?: string | null;
  khoa: Khoa;
}

export interface BenhNhan {
  id: number;
  maBenhNhan: string;
  hoTen: string;
  ngaySinh?: string | null;
  gioiTinh?: string | null;
  soDienThoai?: string | null;
  diaChi?: string | null;
  tienSuBenhAn?: string | null;
}

export interface CaTruc {
  id: number;
  maCa: string;
  tenCa: string;
  gioBatDau?: string | null;
  gioKetThuc?: string | null;
}

export interface LichLamViec {
  id: number;
  ngay: string;
  ghiChu?: string | null;
  bacSi: BacSi;
  phongKham: PhongKham;
}

export interface LanNhapVienLite {
  id: number;
  maNhapVien: string;
  trangThai: 'DANG_DIEU_TRI' | 'DA_XUAT_VIEN';
}

export interface LanKhamBenh {
  id: number;
  maLanKham: string;
  thoiGianKham: string;
  trieuChung?: string | null;
  chanDoan?: string | null;
  trangThai: 'DANG_KHAM' | 'HOAN_THANH';
  benhNhan: BenhNhan;
  lichLamViec?: LichLamViec | null;
  lanNhapVien?: LanNhapVienLite | null;
}

export interface LanNhapVien {
  id: number;
  maNhapVien: string;
  ngayNhap: string;
  ngayXuat?: string | null;
  lyDoNhapVien?: string | null;
  ghiChu?: string | null;
  trangThai: 'DANG_DIEU_TRI' | 'DA_XUAT_VIEN';
  giuongBenh: GiuongBenh;
}

export interface DonThuoc {
  id: number;
  maDonThuoc: string;
  ngayKe: string;
  ghiChu?: string | null;
  lanKhamBenh: LanKhamBenh;
}

export interface Thuoc {
  id: number;
  maThuoc: string;
  tenThuoc: string;
  donViTinh?: string | null;
  donGia?: number | null;
  soLuongTon?: number | null;
  congDung?: string | null;
}

export interface ChiTietThuoc {
  id: number;
  soLuong: number;
  lieuDung?: string | null;
  ghiChu?: string | null;
  donThuoc: DonThuoc;
  thuoc: Thuoc;
}

export interface XetNghiem {
  id: number;
  maXetNghiem: string;
  tenXetNghiem: string;
  loaiXetNghiem?: string | null;
  thoiGian: string;
  ketQua?: string | null;
  trangThai: 'CHO_KET_QUA' | 'CO_KET_QUA';
  lanKhamBenh: LanKhamBenh;
  lichLamViec?: LichLamViec | null;
}

export interface ChamSoc {
  id: number;
  yTa: YTa;
  lanNhapVien: { id: number; maNhapVien: string };
}

export interface LichCaTruc {
  id: number;
  lichLamViec: { id: number; ngay: string; bacSi: { id: number; hoTen: string } };
  caTruc: CaTruc;
}

async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let msg = `Lỗi ${res.status}`;
    try {
      const j = await res.json();
      if (j && j.message) msg = j.message;
    } catch {
      /* giữ thông báo mặc định */
    }
    throw new Error(msg);
  }
  if (res.status === 204) return undefined as unknown as T;
  return res.json();
}

/** Header xác thực (Bearer token lưu ở localStorage) gắn vào mọi request. */
function authHeaders(): Record<string, string> {
  if (typeof window === 'undefined') return {};
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const api = {
  get: <T>(path: string) =>
    fetch(path, { headers: authHeaders() }).then((r) => handle<T>(r)),
  post: <T>(path: string, body: unknown) =>
    fetch(path, { method: 'POST', headers: { 'Content-Type': 'application/json', ...authHeaders() }, body: JSON.stringify(body) }).then((r) => handle<T>(r)),
  put: <T>(path: string, body: unknown) =>
    fetch(path, { method: 'PUT', headers: { 'Content-Type': 'application/json', ...authHeaders() }, body: JSON.stringify(body) }).then((r) => handle<T>(r)),
  del: (path: string) =>
    fetch(path, { method: 'DELETE', headers: authHeaders() }).then((r) => handle<void>(r)),
};

/** Ghép chuỗi truy vấn, bỏ qua tham số rỗng. */
export function qs(params: Record<string, string | number | undefined | null>): string {
  const sp = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null && v !== '') sp.set(k, String(v));
  }
  const s = sp.toString();
  return s ? `?${s}` : '';
}

// ===== Tìm bệnh viện gần nhất trên bản đồ Việt Nam =====

export interface KhoaPhuHop {
  id: number;
  tenKhoa: string;
  moTa?: string | null;
  soBacSi: number;
}

export interface BenhVienGan {
  id: number;
  ten: string;
  diaChi?: string | null;
  soDienThoai?: string | null;
  email?: string | null;
  viDo?: number | null;
  kinhDo?: number | null;
  khoangCachKm?: number | null;
  khoaPhuHop: KhoaPhuHop[];
  soKhoa: number;
  soBacSi: number;
  soYTa: number;
}

export interface BacSiNhanSu {
  id: number;
  maBacSi: string;
  hoTen: string;
  chuyenMon?: string | null;
  hocHam?: string | null;
  soDienThoai?: string | null;
  email?: string | null;
  tenKhoa: string;
}

export interface YTaNhanSu {
  id: number;
  maYTa: string;
  hoTen: string;
  trinhDo?: string | null;
  soDienThoai?: string | null;
  email?: string | null;
  tenKhoa: string;
}

export interface NhanSuBenhVien {
  benhVienId: number;
  tenBenhVien: string;
  bacSi: BacSiNhanSu[];
  yTa: YTaNhanSu[];
}

export const timKiemApi = {
  /** Nhóm bệnh lý gợi ý cho ô tìm kiếm. */
  benhLyGoiY: () => api.get<string[]>('/api/tim-kiem/benh-ly'),
  /** Bệnh viện gần nhất từ vị trí của bệnh nhân, ưu tiên khoa phù hợp với bệnh lý. */
  benhVienGan: (p: { viDo?: number; kinhDo?: number; benhLy?: string }) =>
    api.get<BenhVienGan[]>(`/api/tim-kiem/benh-vien-gan${qs(p)}`),
  /** Nhân sự (bác sĩ + y tá) của một bệnh viện. */
  nhanSu: (id: number) => api.get<NhanSuBenhVien>(`/api/tim-kiem/benh-vien/${id}/nhan-su`),
};
