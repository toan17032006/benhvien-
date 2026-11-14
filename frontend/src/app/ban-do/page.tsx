'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import type * as LeafletNS from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { timKiemApi, type BenhVienGan, type NhanSuBenhVien } from '@/lib/api';
import { Badge, ErrorBox, Loading, PageHeader } from '@/components/ui';

interface ViTri {
  viDo: number;
  kinhDo: number;
}

/** Trang bản đồ Việt Nam: gợi ý bệnh viện gần nhất theo bệnh lý + nhân sự bệnh viện. */
export default function BanDoPage() {
  const mapDiv = useRef<HTMLDivElement>(null);
  const mapRef = useRef<LeafletNS.Map | null>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const LRef = useRef<any>(null);
  const markersRef = useRef<LeafletNS.LayerGroup | null>(null);
  const viTriMarkerRef = useRef<LeafletNS.Marker | null>(null);

  const [all, setAll] = useState<BenhVienGan[]>([]);
  const [ketQua, setKetQua] = useState<BenhVienGan[] | null>(null);
  const [benhLy, setBenhLy] = useState('');
  const [goiY, setGoiY] = useState<string[]>([]);
  const [viTri, setViTri] = useState<ViTri | null>(null);
  const [selected, setSelected] = useState<BenhVienGan | null>(null);
  const [nhanSu, setNhanSu] = useState<NhanSuBenhVien | null>(null);
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

  // ===== Vẽ marker bệnh viện (danh sách hiện tại: kết quả tìm kiếm hoặc toàn bộ) =====
  const dsHienTai = ketQua ?? all;

  useEffect(() => {
    const L = LRef.current;
    const layer = markersRef.current;
    if (!L || !layer) return;
    layer.clearLayers();
    for (const bv of dsHienTai) {
      if (bv.viDo == null || bv.kinhDo == null) continue;
      const icon = L.divIcon({
        className: '',
        html:
          '<div style="display:flex;align-items:center;justify-content:center;width:34px;height:34px;' +
          'border-radius:50%;background:#0ea5e9;color:#fff;font-size:16px;box-shadow:0 2px 6px rgba(2,132,199,.5);' +
          'border:2px solid #fff;">✚</div>',
        iconSize: [34, 34],
        iconAnchor: [17, 17],
      });
      const marker = L.marker([bv.viDo, bv.kinhDo], { icon }).addTo(layer);
      marker.bindTooltip(bv.ten, { direction: 'top', offset: [0, -14] });
      marker.on('click', () => chonBenhVien(bv));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dsHienTai]);

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

  // ===== Chọn bệnh viện trên bản đồ / danh sách =====
  const chonBenhVien = useCallback((bv: BenhVienGan) => {
    setSelected(bv);
    setNhanSu(null);
    const map = mapRef.current;
    if (map && bv.viDo != null && bv.kinhDo != null) {
      map.flyTo([bv.viDo, bv.kinhDo], Math.max(map.getZoom(), 11), { duration: 0.6 });
    }
    timKiemApi
      .nhanSu(bv.id)
      .then(setNhanSu)
      .catch((ex) => setErr(ex instanceof Error ? ex.message : 'Không tải được nhân sự'));
  }, []);

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

  return (
    <div>
      <PageHeader
        title="Bản đồ bệnh viện Việt Nam"
        desc="Gợi ý bệnh viện gần nhất theo bệnh lý — nhấp bản đồ để đặt vị trí, chọn bệnh viện để xem nhân sự"
      />

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

        {/* ===== Cột phải: tìm kiếm + kết quả + nhân sự ===== */}
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
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
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
                : 'Nhấp vào bản đồ hoặc bấm “Vị trí của tôi” để tính khoảng cách.'}
            </p>
          </div>

          {/* Danh sách gợi ý */}
          <div className="rounded-xl border border-slate-200 bg-white shadow-sm">
            <div className="border-b border-slate-100 px-4 py-3 text-sm font-semibold text-slate-700">
              {ketQua != null
                ? `Gợi ý phù hợp (${ketQua.length})`
                : `Bệnh viện trên bản đồ (${all.length})`}
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
              <ul className="max-h-[300px] divide-y divide-slate-100 overflow-y-auto">
                {dsHienTai.map((bv) => (
                  <li key={bv.id}>
                    <button
                      onClick={() => chonBenhVien(bv)}
                      className={`w-full px-4 py-3 text-left transition hover:bg-sky-50 ${
                        selected?.id === bv.id ? 'bg-sky-50' : ''
                      }`}
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-sm font-semibold text-slate-800">{bv.ten}</span>
                        {bv.khoangCachKm != null && (
                          <Badge text={bv.khoangCachKm.toFixed(1) + ' km'} tone={bv.khoangCachKm < 100 ? 'green' : 'sky'} />
                        )}
                      </div>
                      <p className="mt-0.5 truncate text-xs text-slate-500">{bv.diaChi}</p>
                      {bv.khoaPhuHop.length > 0 && (
                        <div className="mt-1.5 flex flex-wrap gap-1">
                          {bv.khoaPhuHop.map((k) => (
                            <Badge key={k.id} text={k.tenKhoa} tone="sky" />
                          ))}
                        </div>
                      )}
                      <p className="mt-1 text-xs text-slate-400">
                        {bv.soBacSi} bác sĩ · {bv.soYTa} y tá · {bv.soKhoa} khoa
                      </p>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Chi tiết bệnh viện đã chọn + nhân sự */}
          {selected && (
            <div className="rounded-xl border border-slate-200 bg-white shadow-sm">
              <div className="border-b border-slate-100 px-4 py-3">
                <h2 className="text-sm font-bold text-slate-800">{selected.ten}</h2>
                <p className="mt-0.5 text-xs text-slate-500">{selected.diaChi}</p>
                {selected.soDienThoai && (
                  <p className="mt-0.5 text-xs text-slate-500">Điện thoại: {selected.soDienThoai}</p>
                )}
                {selected.khoangCachKm != null && (
                  <p className="mt-1 text-xs font-medium text-sky-600">
                    Cách bạn {selected.khoangCachKm.toFixed(1)} km
                  </p>
                )}
              </div>
              {nhanSu == null ? (
                <div className="p-4">
                  <Loading text="Đang tải nhân sự..." />
                </div>
              ) : (
                <div className="max-h-[340px] space-y-4 overflow-y-auto p-4">
                  <div>
                    <h3 className="mb-2 text-xs font-bold uppercase tracking-wide text-slate-500">
                      Bác sĩ ({nhanSu.bacSi.length})
                    </h3>
                    {nhanSu.bacSi.length === 0 ? (
                      <p className="text-xs text-slate-400">Chưa có thông tin bác sĩ.</p>
                    ) : (
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
                            {b.soDienThoai && (
                              <p className="text-xs text-slate-400">ĐT: {b.soDienThoai}</p>
                            )}
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                  <div>
                    <h3 className="mb-2 text-xs font-bold uppercase tracking-wide text-slate-500">
                      Y tá ({nhanSu.yTa.length})
                    </h3>
                    {nhanSu.yTa.length === 0 ? (
                      <p className="text-xs text-slate-400">Chưa có thông tin y tá.</p>
                    ) : (
                      <ul className="space-y-2">
                        {nhanSu.yTa.map((y) => (
                          <li key={y.id} className="rounded-lg bg-slate-50 px-3 py-2">
                            <div className="flex items-center justify-between gap-2">
                              <span className="text-sm font-semibold text-slate-800">{y.hoTen}</span>
                              {y.trinhDo && <Badge text={y.trinhDo} tone="gray" />}
                            </div>
                            <p className="text-xs text-slate-500">{y.tenKhoa}</p>
                            {y.soDienThoai && <p className="text-xs text-slate-400">ĐT: {y.soDienThoai}</p>}
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}
        </aside>
      </div>
    </div>
  );
}
