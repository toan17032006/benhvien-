'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import type * as LeafletNS from 'leaflet';
import 'leaflet/dist/leaflet.css';
import {
  timKiemApi,
  type BenhVienGan,
  type NhanSuBenhVien,
  type DanhGiaBenhVien,
  type DangKyKhamItem,
  type TrangThaiDangKy,
} from '@/lib/api';
import { useAuth } from '@/lib/auth';
import { fmtDateTime, nowForInput } from '@/lib/format';
import { Badge, ErrorBox, Loading, PageHeader } from '@/components/ui';

interface ViTri {
  viDo: number;
  kinhDo: number;
}

function HienSao({ so }: { so: number }) {
  return (
    <span className="text-amber-500" title={`${so}/5 sao`}>
      {'★'.repeat(so)}
      {'☆'.repeat(Math.max(0, 5 - so))}
    </span>
  );
}

const NHAN_TRANG_THAI: Record<TrangThaiDangKy, { text: string; tone: 'yellow' | 'green' | 'gray' }> = {
  CHO_DUYET: { text: 'Chờ duyệt', tone: 'yellow' },
  DA_DUYET: { text: 'Đã duyệt', tone: 'green' },
  DA_HUY: { text: 'Đã hủy', tone: 'gray' },
};

/** Giá trị mặc định cho ô datetime-local: 8h sáng mai. */
function henMacDinh(): string {
  const d = new Date(Date.now() + 24 * 3600 * 1000);
  d.setHours(8, 0, 0, 0);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** Trang bản đồ Việt Nam: gợi ý bệnh viện gần nhất theo bệnh lý — ảnh, giờ mở cửa,
 *  thời gian đi đường, nhân sự và đánh giá; bệnh nhân xem được toàn bộ nội dung. */
export default function BanDoPage() {
  const mapDiv = useRef<HTMLDivElement>(null);
  const mapRef = useRef<LeafletNS.Map | null>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const LRef = useRef<any>(null);
  const markersRef = useRef<LeafletNS.LayerGroup | null>(null);
  const viTriMarkerRef = useRef<LeafletNS.Marker | null>(null);

  const { vaiTro } = useAuth();
  const laBenhNhan = vaiTro === 'BENH_NHAN';

  const [all, setAll] = useState<BenhVienGan[]>([]);
  const [ketQua, setKetQua] = useState<BenhVienGan[] | null>(null);
  const [benhLy, setBenhLy] = useState('');
  const [goiY, setGoiY] = useState<string[]>([]);
  const [viTri, setViTri] = useState<ViTri | null>(null);
  const [selected, setSelected] = useState<BenhVienGan | null>(null);
  const [nhanSu, setNhanSu] = useState<NhanSuBenhVien | null>(null);
  const [danhGia, setDanhGia] = useState<DanhGiaBenhVien | null>(null);
  const [saoChon, setSaoChon] = useState(5);
  const [noiDungDanhGia, setNoiDungDanhGia] = useState('');
  const [dangKyCuaToi, setDangKyCuaToi] = useState<DangKyKhamItem[] | null>(null);
  const [benhLyDk, setBenhLyDk] = useState('');
  const [thoiGianHenDk, setThoiGianHenDk] = useState(henMacDinh());
  const [ghiChuDk, setGhiChuDk] = useState('');
  const [thongBaoDk, setThongBaoDk] = useState('');
  const [loading, setLoading] = useState(true);
  const [dangTim, setDangTim] = useState(false);
  const [err, setErr] = useState('');

  // ===== Khởi tạo bản đồ Leaflet (import động để tránh lỗi SSR) =====
  useEffect(() => {
    let huy = false;
    (async () => {
      try {
        const L = (await import('leaflet')).default;
        if (huy || !mapDiv.current || mapRef.current) return;
        LRef.current = L;
        const map = L.map(mapDiv.current, { zoomControl: true, scrollWheelZoom: true }).setView([16.2, 106.5], 6);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 18,
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        }).addTo(map);
        markersRef.current = L.layerGroup().addTo(map);
        mapRef.current = map;

        // Nhấp vào bản đồ → đặt "vị trí của tôi" tại đó (giống chọn điểm trên Google Maps)
        map.on('click', (e: LeafletNS.LeafletMouseEvent) => {
          setViTri({ viDo: e.latlng.lat, kinhDo: e.latlng.lng });
        });

        // Nạp toàn bộ bệnh viện có tọa độ
        const ds = await timKiemApi.benhVienGan({});
        if (huy) return;
        setAll(ds);
      } catch (ex) {
        if (!huy) setErr(ex instanceof Error ? ex.message : 'Không tải được bản đồ');
      } finally {
        if (!huy) setLoading(false);
      }
    })();
    return () => {
      huy = true;
      mapRef.current?.remove();
      mapRef.current = null;
    };
  }, []);

  useEffect(() => {
    timKiemApi.benhLyGoiY().then(setGoiY).catch(() => {/* gợi ý không bắt buộc */});
  }, []);

  // ===== Vẽ marker bệnh viện (marker được chọn có vòng sáng như Google Maps) =====
  const dsHienTai = ketQua ?? all;

  useEffect(() => {
    const L = LRef.current;
    const layer = markersRef.current;
    if (!L || !layer) return;
    layer.clearLayers();
    for (const bv of dsHienTai) {
      if (bv.viDo == null || bv.kinhDo == null) continue;
      const daChon = selected?.id === bv.id;
      const icon = L.divIcon({
        className: '',
        html:
          '<div style="display:flex;align-items:center;justify-content:center;' +
          (daChon ? 'width:44px;height:44px;font-size:20px;background:#0284c7;border:3px solid #fff;box-shadow:0 0 0 8px rgba(2,132,199,.30);' : 'width:34px;height:34px;font-size:16px;background:#0ea5e9;border:2px solid #fff;box-shadow:0 2px 6px rgba(2,132,199,.5);') +
          'border-radius:50%;color:#fff;">✚</div>',
        iconSize: daChon ? [44, 44] : [34, 34],
        iconAnchor: daChon ? [22, 22] : [17, 17],
      });
      const marker = L.marker([bv.viDo, bv.kinhDo], { icon, zIndexOffset: daChon ? 1000 : 0 }).addTo(layer);
      marker.bindTooltip(
        `${bv.ten} · ${bv.dangMoCua ? 'Đang mở cửa' : 'Đã đóng cửa'}`,
        { direction: 'top', offset: [0, -16] },
      );
      marker.on('click', () => chonBenhVien(bv));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dsHienTai, selected]);

  // ===== Marker "vị trí của tôi" (chấm xanh) =====
  useEffect(() => {
    const L = LRef.current;
    const map = mapRef.current;
    if (!L || !map) return;
    if (viTriMarkerRef.current) {
      viTriMarkerRef.current.remove();
      viTriMarkerRef.current = null;
    }
    if (!viTri) return;
    const icon = L.divIcon({
      className: '',
      html:
        '<div style="width:18px;height:18px;border-radius:50%;background:#2563eb;border:3px solid #fff;' +
        'box-shadow:0 0 0 6px rgba(37,99,235,.25);"></div>',
      iconSize: [18, 18],
      iconAnchor: [9, 9],
    });
    viTriMarkerRef.current = L.marker([viTri.viDo, viTri.kinhDo], { icon }).addTo(map);
  }, [viTri]);

  // ===== Chọn bệnh viện: bản đồ chỉ thẳng vào bệnh viện đó (zoom gần như Google Maps) =====
  const chonBenhVien = useCallback((bv: BenhVienGan) => {
    setSelected(bv);
    setNhanSu(null);
    setDanhGia(null);
    setSaoChon(5);
    setNoiDungDanhGia('');
    setDangKyCuaToi(null);
    setBenhLyDk('');
    setGhiChuDk('');
    setThongBaoDk('');
    setThoiGianHenDk(henMacDinh());
    const map = mapRef.current;
    if (map && bv.viDo != null && bv.kinhDo != null) {
      map.flyTo([bv.viDo, bv.kinhDo], 15, { duration: 1.0 });
    }
    timKiemApi
      .nhanSu(bv.id)
      .then(setNhanSu)
      .catch((ex) => setErr(ex instanceof Error ? ex.message : 'Không tải được nhân sự'));
    timKiemApi
      .danhGia(bv.id)
      .then(setDanhGia)
      .catch((ex) => setErr(ex instanceof Error ? ex.message : 'Không tải được đánh giá'));
    if (vaiTro === 'BENH_NHAN') {
      timKiemApi
        .dangKyCuaToi(bv.id)
        .then(setDangKyCuaToi)
        .catch(() => setDangKyCuaToi([]));
    }
  }, [vaiTro]);

  // ===== Tìm bệnh viện gần nhất theo bệnh lý + vị trí =====
  const tim = useCallback(async (viTriTim?: ViTri) => {
    setErr('');
    setDangTim(true);
    try {
      const vt = viTriTim ?? viTri;
      const ds = await timKiemApi.benhVienGan({
        viDo: vt?.viDo,
        kinhDo: vt?.kinhDo,
        benhLy: benhLy || undefined,
      });
      setKetQua(ds);
      if (ds.length > 0 && ds[0].viDo != null && ds[0].kinhDo != null) {
        mapRef.current?.flyTo([ds[0].viDo, ds[0].kinhDo], 8, { duration: 0.8 });
      }
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : 'Tìm kiếm thất bại');
    } finally {
      setDangTim(false);
    }
  }, [benhLy, viTri]);

  function dungViTriCuaToi() {
    if (!navigator.geolocation) {
      setErr('Trình duyệt không hỗ trợ định vị. Hãy nhấp vào bản đồ để chọn vị trí.');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const vt = { viDo: pos.coords.latitude, kinhDo: pos.coords.longitude };
        setViTri(vt);
        mapRef.current?.flyTo([vt.viDo, vt.kinhDo], 10, { duration: 0.8 });
        void tim(vt);
      },
      () => setErr('Không lấy được vị trí. Hãy nhấp vào bản đồ để chọn vị trí của bạn.'),
    );
  }

  // ===== Bệnh nhân gửi đánh giá =====
  async function guiDanhGia(e: React.FormEvent) {
    e.preventDefault();
    if (!selected) return;
    setErr('');
    try {
      const moi = await timKiemApi.vietDanhGia(selected.id, {
        soSao: saoChon,
        noiDung: noiDungDanhGia,
      });
      setDanhGia(moi);
      setNoiDungDanhGia('');
      setSaoChon(5);
      // Cập nhật điểm trong danh sách đang hiển thị
      const capNhat = (x: BenhVienGan) =>
        x.id === selected.id ? { ...x, trungBinhSao: moi.trungBinhSao, soDanhGia: moi.soDanhGia } : x;
      setKetQua((kq) => (kq ? kq.map(capNhat) : null));
      setAll((a) => a.map(capNhat));
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : 'Gửi đánh giá thất bại');
    }
  }

  // ===== Bệnh nhân gửi đăng ký khám bệnh =====
  async function guiDangKy(e: React.FormEvent) {
    e.preventDefault();
    if (!selected) return;
    setErr('');
    setThongBaoDk('');
    try {
      const moi = await timKiemApi.dangKyKham(selected.id, {
        benhLy: benhLyDk,
        thoiGianHen: thoiGianHenDk,
        ghiChu: ghiChuDk || undefined,
      });
      setThongBaoDk('Đã gửi đăng ký khám — đang chờ bệnh viện duyệt.');
      setBenhLyDk('');
      setGhiChuDk('');
      setThoiGianHenDk(henMacDinh());
      const ds = await timKiemApi.dangKyCuaToi(selected.id);
      setDangKyCuaToi(ds);
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : 'Đăng ký khám thất bại');
    }
  }

  // ===== Bệnh nhân hủy đăng ký khám của chính mình =====
  async function huyDangKy(idDangKy: number) {
    if (!selected) return;
    setErr('');
    setThongBaoDk('');
    try {
      await timKiemApi.huyDangKy(idDangKy);
      setThongBaoDk('Đã hủy đăng ký khám.');
      const ds = await timKiemApi.dangKyCuaToi(selected.id);
      setDangKyCuaToi(ds);
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : 'Hủy đăng ký thất bại');
    }
  }

  const lopInput =
    'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20';

  return (
    <div>
      <PageHeader
        title="Bản đồ bệnh viện Việt Nam"
        desc="Gợi ý bệnh viện gần nhất theo bệnh lý — ảnh, giờ mở cửa, thời gian đi đường, nhân sự và đánh giá"
      />

      {/* Phân quyền theo vai trò */}
      <div className={`mb-4 rounded-lg border px-4 py-2.5 text-sm ${
        laBenhNhan
          ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
          : 'border-slate-200 bg-slate-50 text-slate-600'
      }`}>
        {laBenhNhan
          ? '🟢 Bạn đang xem với vai trò Bệnh nhân: được xem toàn bộ nội dung bệnh viện và viết đánh giá.'
          : '🔵 Bạn đang xem với vai trò Quản trị/Bác sĩ: được xem toàn bộ nội dung bệnh viện (chỉ Bệnh nhân viết đánh giá).'}
      </div>

      {err && (
        <div className="mb-4">
          <ErrorBox message={err} />
        </div>
      )}

      <div className="flex flex-col-reverse gap-4 lg:flex-row">
        {/* ===== Cột trái: bản đồ ===== */}
        <div className="min-w-0 flex-1">
          <div className="overflow-hidden rounded-xl border border-slate-200 shadow-sm">
            <div ref={mapDiv} className="h-[520px] w-full lg:h-[640px]" />
          </div>
        </div>

        {/* ===== Cột phải: tìm kiếm + kết quả + chi tiết ===== */}
        <aside className="w-full shrink-0 space-y-4 lg:w-96">
          {/* Ô tìm kiếm */}
          <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <label htmlFor="benhLy" className="mb-1 block text-sm font-medium text-slate-700">
              Bệnh lý bạn gặp phải
            </label>
            <input
              id="benhLy"
              type="text"
              list="goiYBenhLy"
              value={benhLy}
              onChange={(e) => setBenhLy(e.target.value)}
              placeholder="VD: đau tim, tiểu đường, gãy xương..."
              className={lopInput}
            />
            <datalist id="goiYBenhLy">
              {goiY.map((g) => (
                <option key={g} value={g} />
              ))}
            </datalist>
            <div className="mt-3 flex gap-2">
              <button
                onClick={() => void tim()}
                disabled={dangTim}
                className="flex-1 rounded-lg bg-sky-600 px-3 py-2 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:opacity-50"
              >
                {dangTim ? 'Đang tìm...' : 'Tìm gần nhất'}
              </button>
              <button
                onClick={dungViTriCuaToi}
                className="rounded-lg bg-slate-100 px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-200"
                title="Dùng vị trí hiện tại của bạn"
              >
                Vị trí của tôi
              </button>
            </div>
            <p className="mt-2 text-xs text-slate-400">
              {viTri
                ? `Vị trí của bạn: ${viTri.viDo.toFixed(4)}, ${viTri.kinhDo.toFixed(4)} — nhấp bản đồ để đổi.`
                : 'Nhấp vào bản đồ hoặc bấm “Vị trí của tôi” để tính thời gian đi đường.'}
            </p>
          </div>

          {/* Danh sách gợi ý: ảnh + trạng thái mở cửa + thời gian + sao */}
          <div className="rounded-xl border border-slate-200 bg-white shadow-sm">
            <div className="border-b border-slate-100 px-4 py-3 text-sm font-semibold text-slate-700">
              {ketQua != null ? `Gợi ý phù hợp (${ketQua.length})` : `Bệnh viện trên bản đồ (${all.length})`}
            </div>
            {loading ? (
              <div className="p-4">
                <Loading text="Đang tải bản đồ bệnh viện..." />
              </div>
            ) : dsHienTai.length === 0 ? (
              <p className="px-4 py-6 text-center text-sm text-slate-400">
                Không có bệnh viện phù hợp — thử nhập bệnh lý khác hoặc bỏ điều kiện vị trí.
              </p>
            ) : (
              <ul className="max-h-[340px] divide-y divide-slate-100 overflow-y-auto">
                {dsHienTai.map((bv) => (
                  <li key={bv.id}>
                    <button
                      onClick={() => chonBenhVien(bv)}
                      className={`w-full px-4 py-3 text-left transition hover:bg-sky-50 ${
                        selected?.id === bv.id ? 'bg-sky-50' : ''
                      }`}
                    >
                      <div className="flex gap-3">
                        {bv.anhChinh.length > 0 && (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img
                            src={bv.anhChinh[0]}
                            alt={bv.ten}
                            className="h-14 w-20 shrink-0 rounded-lg object-cover ring-1 ring-slate-200"
                          />
                        )}
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center justify-between gap-2">
                            <span className="truncate text-sm font-semibold text-slate-800">{bv.ten}</span>
                            <Badge
                              text={bv.dangMoCua ? 'Đang mở cửa' : 'Đã đóng cửa'}
                              tone={bv.dangMoCua ? 'green' : 'red'}
                            />
                          </div>
                          <p className="mt-0.5 truncate text-xs text-slate-500">{bv.diaChi}</p>
                          <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-0.5 text-xs">
                            {bv.trungBinhSao != null && (
                              <span>
                                <HienSao so={Math.round(bv.trungBinhSao)} />
                                <span className="ml-1 text-slate-500">
                                  {bv.trungBinhSao} ({bv.soDanhGia})
                                </span>
                              </span>
                            )}
                            {bv.thoiGianPhut != null && (
                              <span className="text-sky-600">
                                🛵 {bv.thoiGianPhut} phút · {bv.quangDuongKm} km{bv.thoiGianUocLuong ? ' (ước lượng)' : ''}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Chi tiết bệnh viện đã chọn: ảnh + giờ + nhân sự + đánh giá */}
          {selected && (
            <div className="rounded-xl border border-slate-200 bg-white shadow-sm">
              {selected.anhChinh.length > 0 && (
                <div className="flex gap-2 overflow-x-auto p-3">
                  {selected.anhChinh.map((url, i) => (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      key={i}
                      src={url}
                      alt={`${selected.ten} — ảnh ${i + 1}`}
                      className="h-24 w-36 shrink-0 rounded-lg object-cover ring-1 ring-slate-200"
                    />
                  ))}
                </div>
              )}
              <div className="border-y border-slate-100 px-4 py-3">
                <div className="flex items-center justify-between gap-2">
                  <h2 className="text-sm font-bold text-slate-800">{selected.ten}</h2>
                  <Badge
                    text={selected.dangMoCua ? 'Đang mở cửa' : 'Đã đóng cửa'}
                    tone={selected.dangMoCua ? 'green' : 'red'}
                  />
                </div>
                <p className="mt-0.5 text-xs text-slate-500">{selected.diaChi}</p>
                <p className="mt-0.5 text-xs text-slate-500">
                  {selected.gioMoCua && selected.gioDongCua
                    ? `Giờ mở cửa: ${selected.gioMoCua} – ${selected.gioDongCua}`
                    : 'Mở cửa 24/24'}
                  {selected.soDienThoai ? ` · ĐT: ${selected.soDienThoai}` : ''}
                </p>
                {selected.khoangCachKm != null && (
                  <p className="mt-1 text-xs font-medium text-sky-600">
                    {selected.thoiGianPhut != null
                      ? `🛵 Đi đến mất khoảng ${selected.thoiGianPhut} phút (${selected.quangDuongKm} km đường bộ)${selected.thoiGianUocLuong ? ' — ước lượng' : ''}`
                      : `Cách bạn ${selected.khoangCachKm} km (đường thẳng)`}
                  </p>
                )}
              </div>

              {/* Nhân sự */}
              {nhanSu == null ? (
                <div className="p-4">
                  <Loading text="Đang tải nhân sự..." />
                </div>
              ) : (
                <div className="border-b border-slate-100 p-4">
                  <h3 className="mb-2 text-xs font-bold uppercase tracking-wide text-slate-500">
                    Nhân sự ({nhanSu.bacSi.length} bác sĩ · {nhanSu.yTa.length} y tá)
                  </h3>
                  <ul className="space-y-2">
                    {nhanSu.bacSi.map((b) => (
                      <li key={b.id} className="rounded-lg bg-slate-50 px-3 py-2">
                        <div className="flex items-center justify-between gap-2">
                          <span className="text-sm font-semibold text-slate-800">{b.hoTen}</span>
                          {b.hocHam && <Badge text={b.hocHam} tone="sky" />}
                        </div>
                        <p className="text-xs text-slate-500">
                          {b.chuyenMon ? b.chuyenMon + ' · ' : ''}{b.tenKhoa}
                        </p>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Đăng ký khám bệnh — phân quyền: chỉ BỆNH NHÂN */}
              <div className="border-b border-slate-100 p-4">
                <h3 className="mb-2 text-xs font-bold uppercase tracking-wide text-slate-500">
                  Đăng ký khám bệnh
                </h3>
                {laBenhNhan ? (
                  <>
                    {thongBaoDk && (
                      <p className="mb-2 rounded-lg bg-emerald-50 px-3 py-2 text-xs font-medium text-emerald-700">
                        {thongBaoDk}
                      </p>
                    )}
                    <form onSubmit={guiDangKy} className="space-y-2 rounded-lg border border-sky-100 bg-sky-50/60 p-3">
                      <div>
                        <label htmlFor="benhLyDk" className="mb-1 block text-xs font-medium text-slate-700">
                          Bệnh lý / triệu chứng <span className="text-rose-500">*</span>
                        </label>
                        <input
                          id="benhLyDk"
                          type="text"
                          required
                          value={benhLyDk}
                          onChange={(e) => setBenhLyDk(e.target.value)}
                          placeholder="VD: đau dạ dày, khám tổng quát..."
                          className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
                        />
                      </div>
                      <div>
                        <label htmlFor="thoiGianHenDk" className="mb-1 block text-xs font-medium text-slate-700">
                          Thời gian hẹn <span className="text-rose-500">*</span>
                        </label>
                        <input
                          id="thoiGianHenDk"
                          type="datetime-local"
                          required
                          min={nowForInput()}
                          value={thoiGianHenDk}
                          onChange={(e) => setThoiGianHenDk(e.target.value)}
                          className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
                        />
                      </div>
                      <div>
                        <label htmlFor="ghiChuDk" className="mb-1 block text-xs font-medium text-slate-700">
                          Ghi chú
                        </label>
                        <input
                          id="ghiChuDk"
                          type="text"
                          value={ghiChuDk}
                          onChange={(e) => setGhiChuDk(e.target.value)}
                          placeholder="Tùy chọn — xin khám buổi sáng, có BHYT..."
                          className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
                        />
                      </div>
                      <button
                        type="submit"
                        className="w-full rounded-lg bg-sky-600 px-3 py-2 text-sm font-semibold text-white transition hover:bg-sky-700"
                      >
                        Gửi đăng ký khám
                      </button>
                    </form>

                    {dangKyCuaToi && dangKyCuaToi.length > 0 && (
                      <div className="mt-3">
                        <p className="mb-1.5 text-xs font-semibold text-slate-600">
                          Đăng ký của bạn tại bệnh viện này ({dangKyCuaToi.length}):
                        </p>
                        <ul className="space-y-2">
                          {dangKyCuaToi.map((dk) => (
                            <li key={dk.id} className="rounded-lg bg-slate-50 px-3 py-2">
                              <div className="flex items-center justify-between gap-2">
                                <span className="text-sm font-medium text-slate-800">
                                  {dk.thoiGianHen ? fmtDateTime(dk.thoiGianHen) : ''}
                                </span>
                                <Badge
                                  text={NHAN_TRANG_THAI[dk.trangThai].text}
                                  tone={NHAN_TRANG_THAI[dk.trangThai].tone}
                                />
                              </div>
                              <p className="text-xs text-slate-500">{dk.benhLy}</p>
                              {dk.ghiChu && <p className="text-xs text-slate-400">Ghi chú: {dk.ghiChu}</p>}
                              {dk.trangThai === 'CHO_DUYET' && (
                                <button
                                  onClick={() => void huyDangKy(dk.id)}
                                  className="mt-1 text-xs font-semibold text-rose-600 hover:underline"
                                >
                                  Hủy đăng ký
                                </button>
                              )}
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </>
                ) : (
                  <p className="rounded-lg bg-slate-50 px-3 py-2 text-xs text-slate-500">
                    Chỉ tài khoản vai trò <b>Bệnh nhân</b> mới có thể đăng ký khám bệnh.
                  </p>
                )}
              </div>

              {/* Đánh giá */}
              {danhGia && (
                <div className="p-4">
                  <div className="mb-3 flex items-center justify-between">
                    <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500">
                      Đánh giá ({danhGia.soDanhGia})
                    </h3>
                    {danhGia.trungBinhSao != null && (
                      <span className="text-sm">
                        <HienSao so={Math.round(danhGia.trungBinhSao)} />
                        <span className="ml-1 font-semibold text-slate-700">{danhGia.trungBinhSao}/5</span>
                      </span>
                    )}
                  </div>
                  {danhGia.danhSach.length === 0 ? (
                    <p className="text-xs text-slate-400">Chưa có đánh giá nào cho bệnh viện này.</p>
                  ) : (
                    <ul className="max-h-52 space-y-2 overflow-y-auto">
                      {danhGia.danhSach.map((d) => (
                        <li key={d.id} className="rounded-lg bg-slate-50 px-3 py-2">
                          <div className="flex items-center justify-between gap-2">
                            <span className="text-sm font-semibold text-slate-800">{d.hoTen}</span>
                            <HienSao so={d.soSao} />
                          </div>
                          <p className="mt-0.5 text-xs text-slate-600">{d.noiDung}</p>
                          <p className="mt-0.5 text-[11px] text-slate-400">
                            {d.thoiGian ? fmtDateTime(d.thoiGian) : ''}
                          </p>
                        </li>
                      ))}
                    </ul>
                  )}

                  {/* Form viết đánh giá — phân quyền: chỉ BỆNH NHÂN */}
                  {laBenhNhan ? (
                    <form onSubmit={guiDanhGia} className="mt-4 space-y-2 rounded-lg border border-sky-100 bg-sky-50/60 p-3">
                      <p className="text-xs font-semibold text-slate-700">Viết đánh giá của bạn:</p>
                      <div className="flex gap-1">
                        {[1, 2, 3, 4, 5].map((s) => (
                          <button
                            key={s}
                            type="button"
                            onClick={() => setSaoChon(s)}
                            className={`text-2xl leading-none transition ${
                              s <= saoChon ? 'text-amber-500' : 'text-slate-300 hover:text-amber-300'
                            }`}
                            aria-label={`${s} sao`}
                          >
                            ★
                          </button>
                        ))}
                      </div>
                      <textarea
                        value={noiDungDanhGia}
                        onChange={(e) => setNoiDungDanhGia(e.target.value)}
                        required
                        rows={2}
                        placeholder="Trải nghiệm của bạn tại bệnh viện này..."
                        className={lopInput}
                      />
                      <button
                        type="submit"
                        className="w-full rounded-lg bg-sky-600 px-3 py-2 text-sm font-semibold text-white transition hover:bg-sky-700"
                      >
                        Gửi đánh giá
                      </button>
                    </form>
                  ) : (
                    <p className="mt-4 rounded-lg bg-slate-50 px-3 py-2 text-xs text-slate-500">
                      Chỉ tài khoản vai trò <b>Bệnh nhân</b> mới có thể viết đánh giá.
                    </p>
                  )}
                </div>
              )}
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}
