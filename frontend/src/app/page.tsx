'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { api, type LanKhamBenh, type LanNhapVien, type LichLamViec } from '@/lib/api';
import { Badge, ErrorBox, Loading, PageHeader } from '@/components/ui';
import { LABEL_KHAM, LABEL_NHAP_VIEN, fmtDateTime, toneForStatus } from '@/lib/format';

interface ThongKe {
  soBenhNhan: number;
  soBacSi: number;
  soYTa: number;
  soKhoa: number;
  soGiuong: number;
  soGiuongTrong: number;
  soGiuongDangSuDung: number;
  soLanKhamHomNay: number;
  soLanKhamDangKham: number;
  soDangNhapVien: number;
  soXetNghiemChoKetQua: number;
  soDonThuoc: number;
  lichLamViecHomNay: { id: number; bacSi: string; chuyenMon: string; phongKham: string; ghiChu?: string }[];
  lanKhamMoiNhat: LanKhamBenh[];
  nhapVienMoiNhat: LanNhapVien[];
}

export default function DashboardPage() {
  const [tk, setTk] = useState<ThongKe | null>(null);
  const [err, setErr] = useState('');

  useEffect(() => {
    api
      .get<ThongKe>('/api/thong-ke')
      .then(setTk)
      .catch((e) => setErr(e instanceof Error ? e.message : 'Lỗi tải số liệu'));
  }, []);

  if (err) return <ErrorBox message={err} />;
  if (!tk) return <Loading text="Đang tải số liệu tổng quan..." />;

  const cards = [
    { label: 'Bệnh nhân', value: tk.soBenhNhan, href: '/benh-nhan', icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z', tone: 'bg-sky-500' },
    { label: 'Bác sĩ', value: tk.soBacSi, href: '/bac-si', icon: 'M13 10V3L4 14h7v7l9-11h-7z', tone: 'bg-violet-500' },
    { label: 'Y tá', value: tk.soYTa, href: '/y-ta', icon: 'M12 4.354a4 4 0 110 5.292', tone: 'bg-emerald-500' },
    { label: 'Khoa', value: tk.soKhoa, href: '/khoa', icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16', tone: 'bg-amber-500' },
    { label: 'Giường trống', value: `${tk.soGiuongTrong}/${tk.soGiuong}`, href: '/giuong-benh', icon: 'M3 18v-6a9 9 0 0118 0v6', tone: 'bg-cyan-500' },
    { label: 'Đang nhập viện', value: tk.soDangNhapVien, href: '/nhap-vien', icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16', tone: 'bg-rose-500' },
    { label: 'Khám hôm nay', value: tk.soLanKhamHomNay, href: '/kham-benh', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2', tone: 'bg-indigo-500' },
    { label: 'XN chờ kết quả', value: tk.soXetNghiemChoKetQua, href: '/xet-nghiem', icon: 'M19.428 15.428a2 2 0 00-1.022-.547', tone: 'bg-fuchsia-500' },
  ];

  return (
    <div>
      <PageHeader title="Bảng điều khiển" desc={`Tổng quan hoạt động bệnh viện — ${new Date().toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' })}`} />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {cards.map((c) => (
          <Link key={c.label} href={c.href} className="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-2xl font-bold text-slate-800">{c.value}</div>
                <div className="mt-1 text-xs font-medium uppercase tracking-wide text-slate-500">{c.label}</div>
              </div>
              <div className={`rounded-xl p-3 text-white ${c.tone}`}>
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d={c.icon} />
                </svg>
              </div>
            </div>
          </Link>
        ))}
      </div>

      <div className="mt-8 grid grid-cols-1 gap-6 xl:grid-cols-2">
        {/* Lịch làm việc hôm nay */}
        <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
            <h2 className="font-semibold text-slate-800">Lịch làm việc hôm nay</h2>
            <Link href="/lich-lam-viec" className="text-xs font-medium text-sky-600 hover:text-sky-700">
              Xem tất cả →
            </Link>
          </div>
          {tk.lichLamViecHomNay.length === 0 ? (
            <div className="px-5 py-10 text-center text-sm text-slate-400">Hôm nay chưa có lịch trực nào</div>
          ) : (
            <ul className="divide-y divide-slate-50">
              {tk.lichLamViecHomNay.map((l) => (
                <li key={l.id} className="flex items-center justify-between px-5 py-3">
                  <div>
                    <div className="text-sm font-medium text-slate-800">{l.bacSi}</div>
                    <div className="text-xs text-slate-500">{l.chuyenMon}</div>
                  </div>
                  <Badge text={l.phongKham} tone="sky" />
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Đang điều trị nội trú */}
        <div className="rounded-2xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
            <h2 className="font-semibold text-slate-800">Đang điều trị nội trú</h2>
            <Link href="/nhap-vien" className="text-xs font-medium text-sky-600 hover:text-sky-700">
              Xem tất cả →
            </Link>
          </div>
          {tk.nhapVienMoiNhat.length === 0 ? (
            <div className="px-5 py-10 text-center text-sm text-slate-400">Không có bệnh nhân nội trú</div>
          ) : (
            <ul className="divide-y divide-slate-50">
              {tk.nhapVienMoiNhat.map((nv) => (
                <li key={nv.id} className="flex items-center justify-between px-5 py-3">
                  <div>
                    <Link href={`/nhap-vien/${nv.id}`} className="text-sm font-medium text-slate-800 hover:text-sky-700">
                      {nv.maNhapVien}
                    </Link>
                    <div className="text-xs text-slate-500">
                      {nv.giuongBenh?.maGiuong} · {nv.giuongBenh?.khoa?.tenKhoa} · vào {fmtDateTime(nv.ngayNhap)}
                    </div>
                  </div>
                  <Badge text={LABEL_NHAP_VIEN[nv.trangThai] ?? nv.trangThai} tone={toneForStatus(nv.trangThai)} />
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {/* Lần khám mới nhất */}
      <div className="mt-6 rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
          <h2 className="font-semibold text-slate-800">Lần khám gần đây</h2>
          <Link href="/kham-benh" className="text-xs font-medium text-sky-600 hover:text-sky-700">
            Xem tất cả →
          </Link>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-5 py-3 font-semibold">Mã lần khám</th>
                <th className="px-5 py-3 font-semibold">Bệnh nhân</th>
                <th className="px-5 py-3 font-semibold">Thời gian</th>
                <th className="px-5 py-3 font-semibold">Chẩn đoán</th>
                <th className="px-5 py-3 font-semibold">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {tk.lanKhamMoiNhat.map((k) => (
                <tr key={k.id} className="transition hover:bg-sky-50/40">
                  <td className="px-5 py-3">
                    <Link href={`/kham-benh/${k.id}`} className="font-medium text-sky-700 hover:underline">
                      {k.maLanKham}
                    </Link>
                  </td>
                  <td className="px-5 py-3">{k.benhNhan?.hoTen}</td>
                  <td className="px-5 py-3 text-slate-500">{fmtDateTime(k.thoiGianKham)}</td>
                  <td className="px-5 py-3 text-slate-500">{k.chanDoan || '—'}</td>
                  <td className="px-5 py-3">
                    <Badge text={LABEL_KHAM[k.trangThai] ?? k.trangThai} tone={toneForStatus(k.trangThai)} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
