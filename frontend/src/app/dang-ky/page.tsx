'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth, type VaiTro } from '@/lib/auth';
import { ErrorBox } from '@/components/ui';
import GoogleButton from '@/components/GoogleButton';

const LA_CHON_VAI_TRO: { value: VaiTro; label: string }[] = [
  { value: 'BENH_NHAN', label: 'Bệnh nhân' },
  { value: 'BAC_SI', label: 'Bác sĩ' },
  { value: 'ADMIN', label: 'Quản trị viên' },
];

export default function DangKyPage() {
  const router = useRouter();
  const { dangKy, dangNhapGoogle } = useAuth();
  const [form, setForm] = useState({
    username: '',
    matKhau: '',
    xacNhanMatKhau: '',
    hoTen: '',
    email: '',
    soDienThoai: '',
    vaiTro: 'BENH_NHAN' as VaiTro,
  });
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);

  function sua(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr('');
    if (form.matKhau !== form.xacNhanMatKhau) {
      setErr('Mật khẩu nhập lại không khớp.');
      return;
    }
    if (form.matKhau.length < 6) {
      setErr('Mật khẩu cần tối thiểu 6 ký tự.');
      return;
    }
    setBusy(true);
    try {
      await dangKy({
        username: form.username,
        matKhau: form.matKhau,
        hoTen: form.hoTen,
        email: form.email || undefined,
        soDienThoai: form.soDienThoai || undefined,
        vaiTro: form.vaiTro,
      });
      router.replace('/');
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : 'Đăng ký thất bại.');
    } finally {
      setBusy(false);
    }
  }

  async function googleDangKy(idToken: string) {
    setErr('');
    try {
      await dangNhapGoogle(idToken);
      router.replace('/');
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : 'Đăng ký bằng Google thất bại.');
    }
  }

  const lopInput =
    'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20';
  const lopLabel = 'mb-1 block text-sm font-medium text-slate-700';

  return (
    <div className="mx-auto max-w-md py-10">
      <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-6 text-center">
          <h1 className="text-xl font-bold text-slate-900">Đăng ký tài khoản</h1>
          <p className="mt-1 text-sm text-slate-500">Hệ thống Quản lý Bệnh viện</p>
        </div>

        {err && (
          <div className="mb-4">
            <ErrorBox message={err} />
          </div>
        )}

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label htmlFor="hoTen" className={lopLabel}>
              Họ và tên <span className="text-rose-500">*</span>
            </label>
            <input id="hoTen" name="hoTen" type="text" required value={form.hoTen} onChange={sua} placeholder="Nguyễn Văn A" className={lopInput} />
          </div>
          <div>
            <label htmlFor="username" className={lopLabel}>
              Tên đăng nhập <span className="text-rose-500">*</span>
            </label>
            <input id="username" name="username" type="text" required value={form.username} onChange={sua} placeholder="VD: nguyenvana" className={lopInput} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label htmlFor="matKhau" className={lopLabel}>
                Mật khẩu <span className="text-rose-500">*</span>
              </label>
              <input id="matKhau" name="matKhau" type="password" required value={form.matKhau} onChange={sua} placeholder="Tối thiểu 6 ký tự" className={lopInput} />
            </div>
            <div>
              <label htmlFor="xacNhanMatKhau" className={lopLabel}>
                Nhập lại mật khẩu <span className="text-rose-500">*</span>
              </label>
              <input id="xacNhanMatKhau" name="xacNhanMatKhau" type="password" required value={form.xacNhanMatKhau} onChange={sua} placeholder="••••••••" className={lopInput} />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label htmlFor="email" className={lopLabel}>
                Email
              </label>
              <input id="email" name="email" type="email" value={form.email} onChange={sua} placeholder="email@gmail.com" className={lopInput} />
            </div>
            <div>
              <label htmlFor="soDienThoai" className={lopLabel}>
                Số điện thoại
              </label>
              <input id="soDienThoai" name="soDienThoai" type="tel" value={form.soDienThoai} onChange={sua} placeholder="09xx xxx xxx" className={lopInput} />
            </div>
          </div>
          <div>
            <label htmlFor="vaiTro" className={lopLabel}>
              Bạn là
            </label>
            <select id="vaiTro" name="vaiTro" value={form.vaiTro} onChange={sua} className={lopInput}>
              {LA_CHON_VAI_TRO.map((v) => (
                <option key={v.value} value={v.value}>
                  {v.label}
                </option>
              ))}
            </select>
          </div>
          <button
            type="submit"
            disabled={busy}
            className="w-full rounded-lg bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {busy ? 'Đang tạo tài khoản...' : 'Đăng ký'}
          </button>
        </form>

        <div className="my-5 flex items-center gap-3 text-xs text-slate-400">
          <span className="h-px flex-1 bg-slate-200" />
          hoặc
          <span className="h-px flex-1 bg-slate-200" />
        </div>

        <GoogleButton text="signup_with" onSuccess={googleDangKy} onError={setErr} />

        <p className="mt-6 text-center text-sm text-slate-600">
          Đã có tài khoản?{' '}
          <Link href="/dang-nhap" className="font-semibold text-sky-600 hover:underline">
            Đăng nhập
          </Link>
        </p>
      </div>
    </div>
  );
}
