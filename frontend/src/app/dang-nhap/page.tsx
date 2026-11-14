'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';
import { ErrorBox } from '@/components/ui';
import GoogleButton from '@/components/GoogleButton';

export default function DangNhapPage() {
  const router = useRouter();
  const { dangNhap, dangNhapGoogle } = useAuth();
  const [username, setUsername] = useState('');
  const [matKhau, setMatKhau] = useState('');
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setErr('');
    setBusy(true);
    try {
      await dangNhap(username, matKhau);
      router.replace('/');
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : 'Đăng nhập thất bại.');
    } finally {
      setBusy(false);
    }
  }

  async function googleDangNhap(idToken: string) {
    setErr('');
    try {
      await dangNhapGoogle(idToken);
      router.replace('/');
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : 'Đăng nhập Google thất bại.');
    }
  }

  return (
    <div className="mx-auto max-w-md py-10">
      <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <div className="mb-6 text-center">
          <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-sky-500/20 ring-1 ring-sky-400/40">
            <svg className="h-7 w-7 text-sky-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m-8-9h16M5 8V6a2 2 0 012-2h10a2 2 0 012 2v2M5 16v2a2 2 0 002 2h10a2 2 0 002-2v-2" />
            </svg>
          </div>
          <h1 className="text-xl font-bold text-slate-900">Đăng nhập hệ thống</h1>
          <p className="mt-1 text-sm text-slate-500">Hệ thống Quản lý Bệnh viện</p>
        </div>

        {err && (
          <div className="mb-4">
            <ErrorBox message={err} />
          </div>
        )}

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label htmlFor="username" className="mb-1 block text-sm font-medium text-slate-700">
              Tên đăng nhập
            </label>
            <input
              id="username"
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="VD: admin"
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
            />
          </div>
          <div>
            <label htmlFor="matKhau" className="mb-1 block text-sm font-medium text-slate-700">
              Mật khẩu
            </label>
            <input
              id="matKhau"
              type="password"
              required
              value={matKhau}
              onChange={(e) => setMatKhau(e.target.value)}
              placeholder="••••••••"
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-500/20"
            />
          </div>
          <button
            type="submit"
            disabled={busy}
            className="w-full rounded-lg bg-sky-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {busy ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>

        <div className="my-5 flex items-center gap-3 text-xs text-slate-400">
          <span className="h-px flex-1 bg-slate-200" />
          hoặc
          <span className="h-px flex-1 bg-slate-200" />
        </div>

        <GoogleButton onSuccess={googleDangNhap} onError={setErr} />

        <p className="mt-6 text-center text-sm text-slate-600">
          Chưa có tài khoản?{' '}
          <Link href="/dang-ky" className="font-semibold text-sky-600 hover:underline">
            Đăng ký ngay
          </Link>
        </p>

        <div className="mt-6 rounded-lg bg-slate-50 p-3 text-xs leading-5 text-slate-500">
          <p className="font-semibold text-slate-600">Tài khoản mẫu (tự tạo khi chạy lần đầu):</p>
          <p>
            Quản trị viên: <code className="text-slate-700">admin / admin123</code>
          </p>
          <p>
            Bác sĩ: <code className="text-slate-700">bacsi / doctor123</code>
          </p>
          <p>
            Bệnh nhân: <code className="text-slate-700">benhnhan / patient123</code>
          </p>
        </div>
      </div>
    </div>
  );
}
