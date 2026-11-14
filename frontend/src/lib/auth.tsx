'use client';

import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { api } from '@/lib/api';

export type VaiTro = 'ADMIN' | 'BAC_SI' | 'BENH_NHAN';

export interface NguoiDungInfo {
  id: number;
  username: string;
  hoTen: string;
  email?: string | null;
  soDienThoai?: string | null;
  vaiTro: VaiTro;
  anhDaiDien?: string | null;
}

export interface AuthResponse {
  token: string;
  nguoiDung: NguoiDungInfo;
}

export interface DangKyData {
  username: string;
  matKhau: string;
  hoTen: string;
  email?: string;
  soDienThoai?: string;
  vaiTro?: VaiTro;
}

interface AuthContextType {
  user: NguoiDungInfo | null;
  vaiTro: VaiTro | null;
  dangNhap: (username: string, matKhau: string) => Promise<void>;
  dangKy: (d: DangKyData) => Promise<void>;
  dangNhapGoogle: (idToken: string) => Promise<void>;
  dangXuat: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const TOKEN_KEY = 'token';
const USER_KEY = 'nguoiDung';
const TRANG_CONG_KAI = ['/dang-nhap', '/dang-ky'];

const NHAN_VAI_TRO: Record<VaiTro, string> = {
  ADMIN: 'Quản trị viên',
  BAC_SI: 'Bác sĩ',
  BENH_NHAN: 'Bệnh nhân',
};

export function tenVaiTro(vaiTro: VaiTro): string {
  return NHAN_VAI_TRO[vaiTro] ?? vaiTro;
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<NguoiDungInfo | null>(null);
  const [ready, setReady] = useState(false);
  const pathname = usePathname();
  const router = useRouter();

  // Khôi phục phiên từ localStorage; token có rồi thì kiểm tra lại qua /api/auth/me
  useEffect(() => {
    let cancelled = false;
    const token = localStorage.getItem(TOKEN_KEY);
    const saved = localStorage.getItem(USER_KEY);
    if (saved) {
      try {
        setUser(JSON.parse(saved) as NguoiDungInfo);
      } catch {
        /* bỏ qua dữ liệu hỏng */
      }
    }
    if (token) {
      api
        .get<NguoiDungInfo>('/api/auth/me')
        .then((nd) => {
          if (cancelled) return;
          setUser(nd);
          localStorage.setItem(USER_KEY, JSON.stringify(nd));
        })
        .catch(() => {
          if (cancelled) return;
          localStorage.removeItem(TOKEN_KEY);
          localStorage.removeItem(USER_KEY);
          setUser(null);
        })
        .finally(() => {
          if (!cancelled) setReady(true);
        });
    } else {
      setReady(true);
    }
    return () => {
      cancelled = true;
    };
  }, []);

  // Chưa đăng nhập mà vào trang quản trị → chuyển sang trang đăng nhập
  useEffect(() => {
    if (!ready) return;
    if (!user && !TRANG_CONG_KAI.includes(pathname ?? '')) {
      router.replace('/dang-nhap');
    }
  }, [ready, user, pathname, router]);

  const luuPhien = useCallback((res: AuthResponse) => {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res.nguoiDung));
    setUser(res.nguoiDung);
  }, []);

  const dangNhap = useCallback(
    async (username: string, matKhau: string) => {
      luuPhien(await api.post<AuthResponse>('/api/auth/login', { username, matKhau }));
    },
    [luuPhien],
  );

  const dangKy = useCallback(
    async (d: DangKyData) => {
      luuPhien(await api.post<AuthResponse>('/api/auth/register', d));
    },
    [luuPhien],
  );

  const dangNhapGoogle = useCallback(
    async (idToken: string) => {
      luuPhien(await api.post<AuthResponse>('/api/auth/google', { idToken }));
    },
    [luuPhien],
  );

  const dangXuat = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
    router.replace('/dang-nhap');
  }, [router]);

  return (
    <AuthContext.Provider
      value={{
        user,
        vaiTro: user?.vaiTro ?? null,
        dangNhap,
        dangKy,
        dangNhapGoogle,
        dangXuat,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth phải dùng bên trong AuthProvider');
  return ctx;
}
