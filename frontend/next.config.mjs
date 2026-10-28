/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // Proxy /api sang Spring Boot — frontend không cần CORS, chỉ cần 1 origin.
  async rewrites() {
    const base = process.env.API_URL || 'http://localhost:8081';
    return [{ source: '/api/:path*', destination: `${base}/api/:path*` }];
  },
};

export default nextConfig;
