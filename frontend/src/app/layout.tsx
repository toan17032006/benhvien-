import type { Metadata } from 'next';
import './globals.css';
import Sidebar from '@/components/Sidebar';

export const metadata: Metadata = {
  title: 'Hệ thống Quản lý Bệnh viện',
  description: 'Ứng dụng quản lý bệnh viện theo sơ đồ ERD — Spring Boot + Next.js',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="vi">
      <body>
        <div className="flex min-h-screen">
          <Sidebar />
          <main className="h-screen flex-1 overflow-y-auto">
            <div className="mx-auto max-w-7xl p-6 lg:p-8">{children}</div>
          </main>
        </div>
      </body>
    </html>
  );
}
