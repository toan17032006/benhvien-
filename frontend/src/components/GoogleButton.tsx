'use client';

import { useEffect, useRef } from 'react';

const GOOGLE_CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID || '';

interface GoogleCredentialResponse {
  credential?: string;
}

interface GoogleIdApi {
  accounts: {
    id: {
      initialize: (config: { client_id: string; callback: (res: GoogleCredentialResponse) => void }) => void;
      renderButton: (parent: HTMLElement, options: Record<string, string | number>) => void;
    };
  };
}

declare global {
  interface Window {
    google?: GoogleIdApi;
  }
}

interface GoogleButtonProps {
  /** Nhãn nút: 'signin_with' (đăng nhập) | 'signup_with' (đăng ký) | 'continue_with' */
  text?: 'signin_with' | 'signup_with' | 'continue_with';
  onSuccess: (idToken: string) => void;
  onError: (message: string) => void;
}

/**
 * Nút "Đăng nhập bằng Google" của Google Identity Services.
 * Chỉ hiện khi đã cấu hình NEXT_PUBLIC_GOOGLE_CLIENT_ID (xem .env.example).
 */
export default function GoogleButton({ text = 'signin_with', onSuccess, onError }: GoogleButtonProps) {
  const divRef = useRef<HTMLDivElement>(null);
  const successRef = useRef(onSuccess);
  const errorRef = useRef(onError);
  successRef.current = onSuccess;
  errorRef.current = onError;

  useEffect(() => {
    if (!GOOGLE_CLIENT_ID) return;
    const el = divRef.current;
    if (!el) return;

    const init = () => {
      const gsi = window.google;
      if (!gsi || !el) return;
      gsi.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: (res) => {
          if (res.credential) successRef.current(res.credential);
          else errorRef.current('Không nhận được token từ Google.');
        },
      });
      gsi.accounts.id.renderButton(el, {
        theme: 'outline',
        size: 'large',
        width: 320,
        text,
        locale: 'vi',
      });
    };

    if (window.google) {
      init();
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.onload = init;
    document.head.appendChild(script);
  }, [text]);

  if (!GOOGLE_CLIENT_ID) {
    return (
      <p className="text-center text-xs text-slate-400">
        Để dùng &ldquo;Đăng nhập bằng Google&rdquo;, hãy cấu hình NEXT_PUBLIC_GOOGLE_CLIENT_ID
        trong <code>frontend/.env.local</code> (xem .env.example).
      </p>
    );
  }
  return <div ref={divRef} className="flex justify-center" />;
}
