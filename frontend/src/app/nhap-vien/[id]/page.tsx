'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { api, type ChamSoc, type DonThuoc, type LanKhamBenh, type LanNhapVien, type YTa } from '@/lib/api';
import { Badge, ErrorBox, Loading, Modal, PageHeader } from '@/components/ui';
import { LABEL_KHAM, LABEL_NHAP_VIEN, fmtDateTime, toneForStatus } from '@/lib/format';

export default function ChiTietNhapVienPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [nv, setNv] = useState<LanNhapVien | null>(null);
  const [chamSocs, setChamSocs] = useState<ChamSoc[]>([]);
  const [yTas, setYTas] = useState<YTa[]>([]);
  const [cacLanKham, setCacLanKham] = useState<LanKhamBenh[]>([]);
  const [donThuocs, setDonThuocs] = useState<DonThuoc[]>([]);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [showThemYT, setShowThemYT] = useState(false);
  const [yTaChon, setYTaChon] = useState('');

  const load = useCallback(async () => {
    try {
      const [n, cs, ys, lks, dts] = await Promise.all([
        api.get<LanNhapVien>(`/api/lan-nhap-vien/${id}`),
        api.get<ChamSoc[]>(`/api/cham-soc?lanNhapVienId=${id}`),
        api.get<YTa[]>('/api/y-ta'),
        api.get<LanKhamBenh[]>(`/api/lan-kham-benh?lanNhapVienId=${id}`),
        api.get<DonThuoc[]>('/api/don-thuoc'),
      ]);
      setNv(n);
      setChamSocs(cs);
      setYTas(ys);
      setCacLanKham(lks);
      const lkIds = new Set(lks.map((k) => k.id));
      setDonThuocs(dts.filter((d) => d.lanKhamBenh && lkIds.has(d.lanKhamBenh.id)));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Không tải được lần nhập viện');
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const xuatVien = async () => {
    if (!window.confirm('Xác nhận xuất viện? Giường sẽ được giải phóng.')) return;
    setSaving(true);
    try {
      await api.post(`/api/lan-nhap-vien/${id}/xuat-vien`, {});
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Xuất viện thất bại');
    } finally {
      setSaving(false);
    }
  };

  const themYTa = async () => {
    setSaving(true);
    try {
      await api.post('/api/cham-soc', { yTaId: Number(yTaChon), lanNhapVienId: Number(id) });
      setShowThemYT(false);
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Phân công thất bại');
    } finally {
      setSaving(false);
    }
  };

  const xoaYTa = async (cs: ChamSoc) => {
    if (!window.confirm(`Bỏ phân công y tá ${cs.yTa?.hoTen}?`)) return;
    try {
      await api.del(`/api/cham-soc/${cs.id}`);
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Xóa thất bại');
    }
  };

  if (!nv && !error) return <Loading />;
  if (!nv) return <ErrorBox message={error} />;

  const dangDieuTri = nv.trangThai === 'DANG_DIEU_TRI';
  const yTaDaPhanCong = new Set(chamSocs.map((c) => c.yTa?.id));

  return (
    <div>
      <PageHeader
        title={`Lần nhập viện ${nv.maNhapVien}`}
        desc={`Vào viện ${fmtDateTime(nv.ngayNhap)}${nv.ngayXuat ? ` · Xuất viện ${fmtDateTime(nv.ngayXuat)}` : ''}`}
        actions={
          <div className="flex gap-2">
            <button onClick={() => router.push('/nhap-vien')} className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50">
              ← Danh sách
            </button>
            {dangDieuTri && (
              <button onClick={xuatVien} disabled={saving} className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700 disabled:opacity-50">
                ✓ Xuất viện
              </button>
            )}
          </div>
        }
      />

      {error && <ErrorBox message={error} />}

      {/* Dải trạng thái */}
      <div className={`mb-6 rounded-2xl border px-5 py-4 ${dangDieuTri ? 'border-amber-200 bg-amber-50' : 'border-slate-200 bg-slate-50'}`}>
        <div className="flex items-center justify-between">
          <div>
            <span className="font-medium">{dangDieuTri ? '🛏️ Bệnh nhân đang điều trị nội trú' : '🏁 Đã xuất viện'}</span>
            <div className="mt-0.5 text-xs text-slate-500">
              Giường {nv.giuongBenh?.maGiuong} · {nv.giuongBenh?.loaiGiuong} · {nv.giuongBenh?.khoa?.tenKhoa}
            </div>
          </div>
          <Badge text={LABEL_NHAP_VIEN[nv.trangThai] ?? nv.trangThai} tone={toneForStatus(nv.trangThai)} />
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Cột trái: thông tin + lý do */}
        <div className="space-y-6">
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <h3 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Thông tin nhập viện</h3>
            <dl className="space-y-2.5 text-sm">
              <div className="flex justify-between gap-3">
                <dt className="text-slate-500">Giường bệnh</dt>
                <dd className="font-medium text-slate-800">{nv.giuongBenh?.maGiuong}</dd>
              </div>
              <div className="flex justify-between gap-3">
                <dt className="text-slate-500">Khoa</dt>
                <dd className="font-medium text-slate-800">{nv.giuongBenh?.khoa?.tenKhoa}</dd>
              </div>
              <div className="flex justify-between gap-3">
                <dt className="text-slate-500">Ngày nhập</dt>
                <dd className="text-slate-700">{fmtDateTime(nv.ngayNhap)}</dd>
              </div>
              <div className="flex justify-between gap-3">
                <dt className="text-slate-500">Ngày xuất</dt>
                <dd className="text-slate-700">{nv.ngayXuat ? fmtDateTime(nv.ngayXuat) : '—'}</dd>
              </div>
            </dl>
            <div className="mt-4 border-t border-slate-100 pt-3">
              <div className="text-xs font-semibold uppercase text-slate-400">Lý do nhập viện</div>
              <p className="mt-1 text-sm text-slate-700">{nv.lyDoNhapVien || '—'}</p>
              {nv.ghiChu && (
                <>
                  <div className="mt-2 text-xs font-semibold uppercase text-slate-400">Ghi chú</div>
                  <p className="mt-1 text-sm text-slate-700">{nv.ghiChu}</p>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Cột phải: y tá chăm sóc */}
        <div className="space-y-6 lg:col-span-2">
          <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
              <h3 className="font-semibold text-slate-800">Y tá chăm sóc ({chamSocs.length})</h3>
              {dangDieuTri && (
                <button onClick={() => { setYTaChon(''); setShowThemYT(true); }} className="rounded-lg bg-sky-600 px-3.5 py-1.5 text-xs font-medium text-white hover:bg-sky-700">
                  + Phân công y tá
                </button>
              )}
            </div>
            {chamSocs.length === 0 ? (
              <div className="px-5 py-8 text-center text-sm text-slate-400">Chưa phân công y tá nào</div>
            ) : (
              <ul className="divide-y divide-slate-50">
                {chamSocs.map((cs) => (
                  <li key={cs.id} className="flex items-center justify-between px-5 py-3">
                    <div className="flex items-center gap-3">
                      <div className="flex h-9 w-9 items-center justify-center rounded-full bg-emerald-100 text-sm font-bold text-emerald-700">
                        {cs.yTa?.hoTen?.split(' ').map((w) => w[0]).slice(-2).join('')}
                      </div>
                      <div>
                        <div className="text-sm font-medium text-slate-800">{cs.yTa?.hoTen}</div>
                        <div className="text-xs text-slate-500">{cs.yTa?.maYTa} · {cs.yTa?.trinhDo} · SĐT {cs.yTa?.soDienThoai}</div>
                      </div>
                    </div>
                    {dangDieuTri && (
                      <button onClick={() => xoaYTa(cs)} className="text-xs font-medium text-rose-600 hover:underline">
                        Bỏ phân công
                      </button>
                    )}
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Các lần khám trong thời gian nhập viện */}
          <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div className="border-b border-slate-100 px-5 py-4">
              <h3 className="font-semibold text-slate-800">Các lần khám trong thời gian điều trị ({cacLanKham.length})</h3>
            </div>
            {cacLanKham.length === 0 ? (
              <div className="px-5 py-8 text-center text-sm text-slate-400">Không có lần khám nào gắn với lần nhập viện này</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                    <tr>
                      <th className="px-5 py-3 font-semibold">Mã</th>
                      <th className="px-5 py-3 font-semibold">Thời gian</th>
                      <th className="px-5 py-3 font-semibold">Chẩn đoán</th>
                      <th className="px-5 py-3 font-semibold">Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {cacLanKham.map((k) => (
                      <tr key={k.id} className="transition hover:bg-sky-50/40">
                        <td className="px-5 py-3">
                          <Link href={`/kham-benh/${k.id}`} className="font-medium text-sky-700 hover:underline">
                            {k.maLanKham}
                          </Link>
                        </td>
                        <td className="px-5 py-3 text-slate-500">{fmtDateTime(k.thoiGianKham)}</td>
                        <td className="px-5 py-3 text-slate-600">{k.chanDoan || k.trieuChung || '—'}</td>
                        <td className="px-5 py-3">
                          <Badge text={LABEL_KHAM[k.trangThai] ?? k.trangThai} tone={toneForStatus(k.trangThai)} />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Đơn thuốc trong thời gian nhập viện */}
          <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div className="border-b border-slate-100 px-5 py-4">
              <h3 className="font-semibold text-slate-800">Đơn thuốc trong thời gian điều trị ({donThuocs.length})</h3>
            </div>
            {donThuocs.length === 0 ? (
              <div className="px-5 py-8 text-center text-sm text-slate-400">Chưa có đơn thuốc nào</div>
            ) : (
              <ul className="divide-y divide-slate-50">
                {donThuocs.map((d) => (
                  <li key={d.id} className="flex items-center justify-between px-5 py-3">
                    <div>
                      <Link href={`/kham-benh/${d.lanKhamBenh?.id}`} className="text-sm font-medium text-sky-700 hover:underline">
                        {d.maDonThuoc}
                      </Link>
                      <span className="ml-3 text-xs text-slate-500">{fmtDateTime(d.ngayKe)}</span>
                      {d.ghiChu && <span className="ml-3 text-xs italic text-slate-400">— {d.ghiChu}</span>}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>

      {/* Modal phân công y tá */}
      <Modal open={showThemYT} title="Phân công y tá chăm sóc" onClose={() => setShowThemYT(false)}>
        <div className="space-y-4">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-700">Chọn y tá *</label>
            <select value={yTaChon} onChange={(e) => setYTaChon(e.target.value)} className="w-full rounded-lg border border-slate-300 px-3.5 py-2 text-sm outline-none focus:border-sky-500 focus:ring-2 focus:ring-sky-100">
              <option value="">— Chọn y tá —</option>
              {yTas.filter((y) => !yTaDaPhanCong.has(y.id)).map((y) => (
                <option key={y.id} value={y.id}>
                  {y.hoTen} — {y.khoa?.tenKhoa} — {y.trinhDo}
                </option>
              ))}
            </select>
          </div>
          <div className="flex justify-end gap-3">
            <button onClick={() => setShowThemYT(false)} className="rounded-lg border border-slate-300 px-4 py-2 text-sm text-slate-600">Hủy</button>
            <button onClick={themYTa} disabled={saving || !yTaChon} className="rounded-lg bg-sky-600 px-5 py-2 text-sm font-medium text-white disabled:opacity-50">
              Phân công
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
