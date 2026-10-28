'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { api, type ChiTietThuoc, type DonThuoc, type GiuongBenh, type LanKhamBenh, type LichLamViec, type Thuoc, type XetNghiem } from '@/lib/api';
import { Badge, ErrorBox, Loading, Modal, PageHeader } from '@/components/ui';
import { LABEL_KHAM, LABEL_XET_NGHIEM, fmtDateTime, nowForInput, toneForStatus } from '@/lib/format';

export default function ChiTietLanKhamPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [kham, setKham] = useState<LanKhamBenh | null>(null);
  const [donThuocs, setDonThuocs] = useState<DonThuoc[]>([]);
  const [xetNghiems, setXetNghiems] = useState<XetNghiem[]>([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  // form chỉnh lần khám
  const [form, setForm] = useState<{ thoiGianKham: string; trangThai: string; trieuChung: string; chanDoan: string; lichLamViecId: string }>({
    thoiGianKham: '',
    trangThai: 'DANG_KHAM',
    trieuChung: '',
    chanDoan: '',
    lichLamViecId: '',
  });
  const [lichs, setLichs] = useState<LichLamViec[]>([]);

  // nhập viện
  const [giuongsTrong, setGiuongsTrong] = useState<GiuongBenh[]>([]);
  const [showNhapVien, setShowNhapVien] = useState(false);
  const [nhapVienForm, setNhapVienForm] = useState({ giuongBenhId: '', lyDoNhapVien: '', ghiChu: '' });

  // đơn thuốc + chi tiết
  const [showKeDon, setShowKeDon] = useState(false);
  const [keDonGhiChu, setKeDonGhiChu] = useState('');
  const [expandedDon, setExpandedDon] = useState<number | null>(null);
  const [chiTiets, setChiTiets] = useState<ChiTietThuoc[]>([]);
  const [thuocs, setThuocs] = useState<Thuoc[]>([]);
  const [showChiTiet, setShowChiTiet] = useState<ChiTietThuoc | 'new' | null>(null);
  const [ctForm, setCtForm] = useState({ thuocId: '', soLuong: '', lieuDung: '', ghiChu: '' });

  // xét nghiệm
  const [showXn, setShowXn] = useState<XetNghiem | 'new' | null>(null);
  const [xnForm, setXnForm] = useState({ tenXetNghiem: '', loaiXetNghiem: '', ketQua: '', trangThai: 'CHO_KET_QUA' });

  const loadKham = useCallback(async () => {
    try {
      const [k, dts, xns, ls] = await Promise.all([
        api.get<LanKhamBenh>(`/api/lan-kham-benh/${id}`),
        api.get<DonThuoc[]>(`/api/don-thuoc?lanKhamBenhId=${id}`),
        api.get<XetNghiem[]>(`/api/xet-nghiem?lanKhamBenhId=${id}`),
        api.get<LichLamViec[]>('/api/lich-lam-viec'),
      ]);
      setKham(k);
      setDonThuocs(dts);
      setXetNghiems(xns);
      setLichs(ls);
      setForm({
        thoiGianKham: (k.thoiGianKham ?? '').slice(0, 16),
        trangThai: k.trangThai,
        trieuChung: k.trieuChung ?? '',
        chanDoan: k.chanDoan ?? '',
        lichLamViecId: k.lichLamViec ? String(k.lichLamViec.id) : '',
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Không tải được lần khám');
    }
  }, [id]);

  useEffect(() => {
    loadKham();
    api.get<Thuoc[]>('/api/thuoc').then(setThuocs).catch(() => {});
  }, [loadKham]);

  const loadChiTiet = async (donId: number) => {
    try {
      setChiTiets(await api.get<ChiTietThuoc[]>(`/api/chi-tiet-thuoc?donThuocId=${donId}`));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Lỗi tải chi tiết đơn');
    }
  };

  const saveKham = async () => {
    setSaving(true);
    setError('');
    try {
      await api.put(`/api/lan-kham-benh/${id}`, {
        maLanKham: kham?.maLanKham,
        thoiGianKham: form.thoiGianKham || null,
        trangThai: form.trangThai,
        trieuChung: form.trieuChung,
        chanDoan: form.chanDoan,
        benhNhanId: kham?.benhNhan?.id,
        lichLamViecId: form.lichLamViecId ? Number(form.lichLamViecId) : null,
        lanNhapVienId: kham?.lanNhapVien?.id ?? null,
      });
      await loadKham();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Lưu thất bại');
    } finally {
      setSaving(false);
    }
  };

  const openNhapVien = async () => {
    try {
      setGiuongsTrong(await api.get<GiuongBenh[]>('/api/giuong-benh?trangThai=TRONG'));
      setNhapVienForm({ giuongBenhId: '', lyDoNhapVien: '', ghiChu: '' });
      setShowNhapVien(true);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Lỗi tải giường trống');
    }
  };

  const submitNhapVien = async () => {
    setSaving(true);
    setError('');
    try {
      await api.post(`/api/lan-kham-benh/${id}/nhap-vien`, {
        giuongBenhId: Number(nhapVienForm.giuongBenhId),
        lyDoNhapVien: nhapVienForm.lyDoNhapVien,
        ghiChu: nhapVienForm.ghiChu || null,
      });
      setShowNhapVien(false);
      await loadKham();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Nhập viện thất bại');
    } finally {
      setSaving(false);
    }
  };

  const keDon = async () => {
    setSaving(true);
    try {
      const dt = await api.post<DonThuoc>('/api/don-thuoc', {
        lanKhamBenhId: Number(id),
        ngayKe: nowForInput(),
        ghiChu: keDonGhiChu || null,
      });
      setShowKeDon(false);
      setKeDonGhiChu('');
      await loadKham();
      setExpandedDon(dt.id);
      loadChiTiet(dt.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Kê đơn thất bại');
    } finally {
      setSaving(false);
    }
  };

  const openChiTiet = (ct: ChiTietThuoc | 'new') => {
    setShowChiTiet(ct);
    if (ct === 'new') setCtForm({ thuocId: '', soLuong: '', lieuDung: '', ghiChu: '' });
    else setCtForm({ thuocId: String(ct.thuoc.id), soLuong: String(ct.soLuong), lieuDung: ct.lieuDung ?? '', ghiChu: ct.ghiChu ?? '' });
  };

  const saveChiTiet = async () => {
    if (!expandedDon) return;
    setSaving(true);
    try {
      const payload = {
        soLuong: Number(ctForm.soLuong),
        lieuDung: ctForm.lieuDung || null,
        ghiChu: ctForm.ghiChu || null,
        donThuocId: expandedDon,
        thuocId: Number(ctForm.thuocId),
      };
      if (showChiTiet === 'new') await api.post('/api/chi-tiet-thuoc', payload);
      else await api.put(`/api/chi-tiet-thuoc/${(showChiTiet as ChiTietThuoc).id}`, payload);
      setShowChiTiet(null);
      loadChiTiet(expandedDon);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Lưu thất bại');
    } finally {
      setSaving(false);
    }
  };

  const xoaChiTiet = async (ct: ChiTietThuoc) => {
    if (!window.confirm('Xóa thuốc này khỏi đơn?')) return;
    try {
      await api.del(`/api/chi-tiet-thuoc/${ct.id}`);
      loadChiTiet(ct.donThuoc.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Xóa thất bại');
    }
  };

  const openXn = (x: XetNghiem | 'new') => {
    setShowXn(x);
    if (x === 'new') setXnForm({ tenXetNghiem: '', loaiXetNghiem: '', ketQua: '', trangThai: 'CHO_KET_QUA' });
    else setXnForm({ tenXetNghiem: x.tenXetNghiem, loaiXetNghiem: x.loaiXetNghiem ?? '', ketQua: x.ketQua ?? '', trangThai: x.trangThai });
  };

  const saveXn = async () => {
    setSaving(true);
    try {
      const payload = {
        tenXetNghiem: xnForm.tenXetNghiem,
        loaiXetNghiem: xnForm.loaiXetNghiem || null,
        ketQua: xnForm.ketQua || null,
        trangThai: xnForm.trangThai,
        lanKhamBenhId: Number(id),
        thoiGian: nowForInput(),
      };
      if (showXn === 'new') await api.post('/api/xet-nghiem', payload);
      else await api.put(`/api/xet-nghiem/${(showXn as XetNghiem).id}`, payload);
      setShowXn(null);
      loadKham();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Lưu thất bại');
    } finally {
      setSaving(false);
    }
  };

  const xoaXn = async (x: XetNghiem) => {
    if (!window.confirm('Xóa xét nghiệm này?')) return;
    try {
      await api.del(`/api/xet-nghiem/${x.id}`);
      loadKham();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Xóa thất bại');
    }
  };

  if (!kham && !error) return <Loading />;
  if (!kham) return <ErrorBox message={error} />;

  const inputCls = 'w-full rounded-lg border border-slate-300 px-3.5 py-2 text-sm outline-none transition focus:border-sky-500 focus:ring-2 focus:ring-sky-100';

  return (
    <div>
      <PageHeader
        title={`Lần khám ${kham.maLanKham}`}
        desc={`${fmtDateTime(kham.thoiGianKham)} · ${kham.benhNhan?.hoTen} (${kham.benhNhan?.maBenhNhan})`}
        actions={
          <div className="flex gap-2">
            <button onClick={() => router.push('/kham-benh')} className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50">
              ← Danh sách
            </button>
            {!kham.lanNhapVien && (
              <button onClick={openNhapVien} className="rounded-lg bg-rose-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-rose-700">
                ➕ Nhập viện
              </button>
            )}
          </div>
        }
      />

      {error && <ErrorBox message={error} />}

      {/* Trạng thái nhập viện */}
      {kham.lanNhapVien && (
        <div className="mb-6 flex items-center justify-between rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4">
          <div>
            <div className="font-medium text-rose-800">Bệnh nhân đã được nhập viện — {kham.lanNhapVien.maNhapVien}</div>
            <div className="text-xs text-rose-600">Xem chi tiết điều trị nội trú, giường bệnh và y tá chăm sóc</div>
          </div>
          <Link href={`/nhap-vien/${kham.lanNhapVien.id}`} className="rounded-lg bg-rose-600 px-4 py-2 text-sm font-medium text-white hover:bg-rose-700">
            Xem lần nhập viện →
          </Link>
        </div>
      )}

      {/* Thông tin bệnh nhân */}
      <div className="mb-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Bệnh nhân</h3>
          <div className="text-lg font-bold text-slate-800">{kham.benhNhan?.hoTen}</div>
          <div className="mt-2 space-y-1 text-sm text-slate-500">
            <div>Mã: {kham.benhNhan?.maBenhNhan}</div>
            <div>SĐT: {kham.benhNhan?.soDienThoai || '—'}</div>
            {kham.benhNhan?.tienSuBenhAn && <div>Tiền sử: {kham.benhNhan.tienSuBenhAn}</div>}
          </div>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Bác sĩ khám</h3>
          {kham.lichLamViec ? (
            <div>
              <div className="text-lg font-bold text-slate-800">{kham.lichLamViec.bacSi?.hoTen}</div>
              <div className="mt-2 space-y-1 text-sm text-slate-500">
                <div>{kham.lichLamViec.bacSi?.chuyenMon}</div>
                <div>Phòng: {kham.lichLamViec.phongKham?.tenPhong}</div>
                <div>Khoa: {kham.lichLamViec.bacSi?.khoa?.tenKhoa}</div>
              </div>
            </div>
          ) : (
            <div className="text-sm text-slate-400">Chưa gắn lịch làm việc (khám ngoài giờ trực)</div>
          )}
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Trạng thái</h3>
          <Badge text={LABEL_KHAM[kham.trangThai] ?? kham.trangThai} tone={toneForStatus(kham.trangThai)} />
          <div className="mt-3 text-xs text-slate-400">Cập nhật biểu đồ khám, chẩn đoán và trạng thái bên dưới</div>
        </div>
      </div>

      {/* Biểu đồ khám */}
      <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <h3 className="mb-4 font-semibold text-slate-800">Thông tin khám bệnh</h3>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Thời gian khám</label>
            <input type="datetime-local" value={form.thoiGianKham} onChange={(e) => setForm({ ...form, thoiGianKham: e.target.value })} className={inputCls} />
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Trạng thái</label>
            <select value={form.trangThai} onChange={(e) => setForm({ ...form, trangThai: e.target.value })} className={inputCls}>
              <option value="DANG_KHAM">Đang khám</option>
              <option value="HOAN_THANH">Hoàn thành</option>
            </select>
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Lịch làm việc (bác sĩ trực)</label>
            <select value={form.lichLamViecId} onChange={(e) => setForm({ ...form, lichLamViecId: e.target.value })} className={inputCls}>
              <option value="">— Không gắn lịch —</option>
              {lichs.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.ngay} — {l.bacSi?.hoTen} — {l.phongKham?.tenPhong}
                </option>
              ))}
            </select>
          </div>
          <div className="sm:col-span-2">
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Triệu chứng</label>
            <textarea rows={2} value={form.trieuChung} onChange={(e) => setForm({ ...form, trieuChung: e.target.value })} className={inputCls} />
          </div>
          <div className="sm:col-span-2">
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Chẩn đoán</label>
            <textarea rows={2} value={form.chanDoan} onChange={(e) => setForm({ ...form, chanDoan: e.target.value })} className={inputCls} />
          </div>
        </div>
        <div className="mt-4 flex justify-end">
          <button onClick={saveKham} disabled={saving} className="rounded-lg bg-sky-600 px-5 py-2 text-sm font-medium text-white shadow-sm hover:bg-sky-700 disabled:opacity-50">
            {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
        </div>
      </div>

      {/* Đơn thuốc */}
      <div className="mb-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
          <h3 className="font-semibold text-slate-800">Đơn thuốc ({donThuocs.length})</h3>
          <button onClick={() => setShowKeDon(true)} className="rounded-lg bg-sky-600 px-3.5 py-1.5 text-xs font-medium text-white hover:bg-sky-700">
            + Kê đơn mới
          </button>
        </div>
        {donThuocs.length === 0 ? (
          <div className="px-5 py-8 text-center text-sm text-slate-400">Chưa có đơn thuốc</div>
        ) : (
          <div className="divide-y divide-slate-100">
            {donThuocs.map((dt) => (
              <div key={dt.id}>
                <button
                  onClick={() => {
                    if (expandedDon === dt.id) setExpandedDon(null);
                    else {
                      setExpandedDon(dt.id);
                      loadChiTiet(dt.id);
                    }
                  }}
                  className="flex w-full items-center justify-between px-5 py-3 text-left transition hover:bg-slate-50"
                >
                  <div>
                    <span className="font-medium text-slate-800">{dt.maDonThuoc}</span>
                    <span className="ml-3 text-xs text-slate-500">{fmtDateTime(dt.ngayKe)}</span>
                    {dt.ghiChu && <span className="ml-3 text-xs italic text-slate-400">— {dt.ghiChu}</span>}
                  </div>
                  <span className="text-xs text-sky-600">{expandedDon === dt.id ? 'Thu gọn ▲' : 'Chi tiết ▼'}</span>
                </button>
                {expandedDon === dt.id && (
                  <div className="bg-slate-50/60 px-5 pb-4">
                    <table className="w-full text-left text-sm">
                      <thead className="text-xs uppercase text-slate-500">
                        <tr>
                          <th className="py-2 pr-3 font-semibold">Thuốc</th>
                          <th className="py-2 pr-3 font-semibold">SL</th>
                          <th className="py-2 pr-3 font-semibold">Liều dùng</th>
                          <th className="py-2 pr-3 font-semibold">Ghi chú thuốc</th>
                          <th className="py-2 text-right font-semibold">Thao tác</th>
                        </tr>
                      </thead>
                      <tbody>
                        {chiTiets.map((ct) => (
                          <tr key={ct.id} className="border-t border-slate-200">
                            <td className="py-2 pr-3">
                              <div className="font-medium text-slate-800">{ct.thuoc?.tenThuoc}</div>
                              <div className="text-xs text-slate-400">{ct.thuoc?.donViTinh}</div>
                            </td>
                            <td className="py-2 pr-3">{ct.soLuong}</td>
                            <td className="py-2 pr-3 text-slate-600">{ct.lieuDung || '—'}</td>
                            <td className="py-2 pr-3 text-slate-600">{ct.ghiChu || '—'}</td>
                            <td className="py-2 text-right">
                              <button onClick={() => openChiTiet(ct)} className="mr-2 text-xs font-medium text-sky-700 hover:underline">
                                Sửa
                              </button>
                              <button onClick={() => xoaChiTiet(ct)} className="text-xs font-medium text-rose-600 hover:underline">
                                Xóa
                              </button>
                            </td>
                          </tr>
                        ))}
                        {chiTiets.length === 0 && (
                          <tr>
                            <td colSpan={5} className="py-3 text-center text-xs text-slate-400">
                              Chưa có thuốc trong đơn
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                    <button onClick={() => openChiTiet('new')} className="mt-2 rounded-lg border border-sky-300 bg-white px-3 py-1.5 text-xs font-medium text-sky-700 hover:bg-sky-50">
                      + Thêm thuốc vào đơn
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Xét nghiệm */}
      <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
          <h3 className="font-semibold text-slate-800">Xét nghiệm ({xetNghiems.length})</h3>
          <button onClick={() => openXn('new')} className="rounded-lg bg-sky-600 px-3.5 py-1.5 text-xs font-medium text-white hover:bg-sky-700">
            + Thêm xét nghiệm
          </button>
        </div>
        {xetNghiems.length === 0 ? (
          <div className="px-5 py-8 text-center text-sm text-slate-400">Chưa có xét nghiệm</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-5 py-3 font-semibold">Mã</th>
                  <th className="px-5 py-3 font-semibold">Tên</th>
                  <th className="px-5 py-3 font-semibold">Loại</th>
                  <th className="px-5 py-3 font-semibold">Kết quả</th>
                  <th className="px-5 py-3 font-semibold">Trạng thái</th>
                  <th className="px-5 py-3 text-right font-semibold">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {xetNghiems.map((x) => (
                  <tr key={x.id}>
                    <td className="px-5 py-3 font-medium text-slate-700">{x.maXetNghiem}</td>
                    <td className="px-5 py-3">{x.tenXetNghiem}</td>
                    <td className="px-5 py-3 text-slate-500">{x.loaiXetNghiem || '—'}</td>
                    <td className="max-w-xs px-5 py-3 text-slate-500">{x.ketQua || '—'}</td>
                    <td className="px-5 py-3">
                      <Badge text={LABEL_XET_NGHIEM[x.trangThai] ?? x.trangThai} tone={toneForStatus(x.trangThai)} />
                    </td>
                    <td className="px-5 py-3 text-right">
                      <button onClick={() => openXn(x)} className="mr-2 text-xs font-medium text-sky-700 hover:underline">
                        Sửa
                      </button>
                      <button onClick={() => xoaXn(x)} className="text-xs font-medium text-rose-600 hover:underline">
                        Xóa
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ===== Các modal ===== */}
      <Modal open={showNhapVien} title="Nhập viện từ lần khám" onClose={() => setShowNhapVien(false)}>
        <div className="space-y-4">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Giường trống</label>
            <select value={nhapVienForm.giuongBenhId} onChange={(e) => setNhapVienForm({ ...nhapVienForm, giuongBenhId: e.target.value })} className={inputCls}>
              <option value="">— Chọn giường —</option>
              {giuongsTrong.map((g) => (
                <option key={g.id} value={g.id}>
                  {g.maGiuong} — {g.loaiGiuong} — {g.khoa?.tenKhoa}
                </option>
              ))}
            </select>
            {giuongsTrong.length === 0 && <p className="mt-1 text-xs text-rose-500">Không còn giường trống — hãy thêm giường ở mục Giường bệnh.</p>}
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Lý do nhập viện *</label>
            <textarea rows={2} value={nhapVienForm.lyDoNhapVien} onChange={(e) => setNhapVienForm({ ...nhapVienForm, lyDoNhapVien: e.target.value })} className={inputCls} />
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Ghi chú</label>
            <input type="text" value={nhapVienForm.ghiChu} onChange={(e) => setNhapVienForm({ ...nhapVienForm, ghiChu: e.target.value })} className={inputCls} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button onClick={() => setShowNhapVien(false)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-600">
              Hủy
            </button>
            <button onClick={submitNhapVien} disabled={saving || !nhapVienForm.giuongBenhId || !nhapVienForm.lyDoNhapVien.trim()} className="rounded-lg bg-rose-600 px-5 py-2 text-sm font-medium text-white disabled:opacity-50">
              Xác nhận nhập viện
            </button>
          </div>
        </div>
      </Modal>

      <Modal open={showKeDon} title="Kê đơn thuốc mới" onClose={() => setShowKeDon(false)}>
        <div className="space-y-4">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Ghi chú</label>
            <input type="text" value={keDonGhiChu} onChange={(e) => setKeDonGhiChu(e.target.value)} placeholder="VD: Uống sau ăn" className={inputCls} />
          </div>
          <p className="text-xs text-slate-400">Sau khi tạo đơn, bạn sẽ thêm từng thuốc vào đơn ngay bên dưới.</p>
          <div className="flex justify-end gap-3">
            <button onClick={() => setShowKeDon(false)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-600">
              Hủy
            </button>
            <button onClick={keDon} disabled={saving} className="rounded-lg bg-sky-600 px-5 py-2 text-sm font-medium text-white disabled:opacity-50">
              Tạo đơn
            </button>
          </div>
        </div>
      </Modal>

      <Modal open={showChiTiet !== null} title={showChiTiet === 'new' ? 'Thêm thuốc vào đơn' : 'Sửa thuốc trong đơn'} onClose={() => setShowChiTiet(null)}>
        <div className="space-y-4">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Thuốc *</label>
            <select value={ctForm.thuocId} onChange={(e) => setCtForm({ ...ctForm, thuocId: e.target.value })} className={inputCls}>
              <option value="">— Chọn thuốc —</option>
              {thuocs.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.tenThuoc} ({t.donViTinh})
                </option>
              ))}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1.5 block text-sm font-medium text-slate-700">Số lượng *</label>
              <input type="number" min={1} value={ctForm.soLuong} onChange={(e) => setCtForm({ ...ctForm, soLuong: e.target.value })} className={inputCls} />
            </div>
            <div>
              <label className="mb-1.5 block text-sm font-medium text-slate-700">Liều dùng</label>
              <input type="text" value={ctForm.lieuDung} onChange={(e) => setCtForm({ ...ctForm, lieuDung: e.target.value })} placeholder="1 viên × 3 lần/ngày" className={inputCls} />
            </div>
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Ghi chú thuốc</label>
            <input type="text" value={ctForm.ghiChu} onChange={(e) => setCtForm({ ...ctForm, ghiChu: e.target.value })} className={inputCls} />
          </div>
          <div className="flex justify-end gap-3">
            <button onClick={() => setShowChiTiet(null)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-600">
              Hủy
            </button>
            <button onClick={saveChiTiet} disabled={saving || !ctForm.thuocId || !ctForm.soLuong} className="rounded-lg bg-sky-600 px-5 py-2 text-sm font-medium text-white disabled:opacity-50">
              Lưu
            </button>
          </div>
        </div>
      </Modal>

      <Modal open={showXn !== null} title={showXn === 'new' ? 'Thêm xét nghiệm' : 'Cập nhật xét nghiệm'} onClose={() => setShowXn(null)}>
        <div className="space-y-4">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Tên xét nghiệm *</label>
            <input type="text" value={xnForm.tenXetNghiem} onChange={(e) => setXnForm({ ...xnForm, tenXetNghiem: e.target.value })} className={inputCls} />
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Loại</label>
            <input type="text" value={xnForm.loaiXetNghiem} onChange={(e) => setXnForm({ ...xnForm, loaiXetNghiem: e.target.value })} placeholder="Máu / Nước tiểu / X-quang..." className={inputCls} />
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Trạng thái</label>
            <select value={xnForm.trangThai} onChange={(e) => setXnForm({ ...xnForm, trangThai: e.target.value })} className={inputCls}>
              <option value="CHO_KET_QUA">Chờ kết quả</option>
              <option value="CO_KET_QUA">Có kết quả</option>
            </select>
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Kết quả</label>
            <textarea rows={3} value={xnForm.ketQua} onChange={(e) => setXnForm({ ...xnForm, ketQua: e.target.value })} className={inputCls} />
          </div>
          <div className="flex justify-end gap-3">
            <button onClick={() => setShowXn(null)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-600">
              Hủy
            </button>
            <button onClick={saveXn} disabled={saving || !xnForm.tenXetNghiem.trim()} className="rounded-lg bg-sky-600 px-5 py-2 text-sm font-medium text-white disabled:opacity-50">
              Lưu
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
