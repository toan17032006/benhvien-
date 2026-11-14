# AGENT.md — Sổ tay vận hành cho AI Agent

> Dự án: **Hệ thống Quản lý Bệnh viện** — bám sát code trên `github.com/toan17032006/benhvien-`.
> - Nhánh **main**: bản gốc 17 bảng ERD (không có đăng nhập).
> - Nhánh **hospital**: bản gốc + **đăng nhập/đăng ký JWT + Đăng nhập bằng Google** (không xóa gì của main).
> Tài liệu chi tiết khác: `README.md` (cách chạy từng bước) · `PRD.md, ARCHITECTURE.md, DESIGN.md, RULES.md, TASKS.md, PROGRESS.md` (bộ 6 file gốc, mô tả bản local cũ — chỉ mang tính tham khảo lịch sử, **không** mô tả repo này).

---

## 1. Bối cảnh 30 giây

- **Monorepo**: `backend/` (Spring Boot) + `frontend/` (Next.js) + script chạy sẵn (`start-all.bat`, `start-backend.bat`, `start-frontend.bat`…).
- **Backend**: Spring Boot 4.1.2, **Java 17**, package `com.benhvien`, JPA/Hibernate (`ddl-auto=update`), **H2 file** mặc định (`backend/data/`) — **không cần cài MySQL/Docker**; MySQL tùy chọn qua profile `mysql`. Port **8081**.
- **Frontend**: Next.js 14 (App Router), React 18, TypeScript strict, **Tailwind CSS 3** (có `tailwind.config.ts`), icon = **inline SVG** (KHÔNG dùng lucide). Port **3000**, gọi API qua **proxy `/api`** của Next (xem `next.config.mjs`) — không cần CORS.
- **Auth (chỉ có ở nhánh hospital)**: JWT HS256 tự triển khai (JDK) + BCrypt + Spring Security; token + user lưu `localStorage` frontend; **Đăng nhập bằng Google** qua Google Identity Services, backend xác minh ID token bằng endpoint `tokeninfo` của Google (không thêm thư viện ngoài).
- **Bản đồ bệnh viện (nhánh hospital)**: trang `/ban-do` dùng **Leaflet + OpenStreetMap** (duy nhất thư viện ngoài bổ sung — `leaflet` + `@types/leaflet`); backend `/api/tim-kiem/*` tìm bệnh viện gần nhất theo bệnh lý (Haversine, ghép bệnh lý → khoa/chuyên môn); `BenhVien` có `viDo/kinhDo`; seed 10 bệnh viện thật trên bản đồ.
- **Tài khoản mẫu seed tự động**: `admin/admin123` (ADMIN), `bacsi/doctor123` (BAC_SI), `benhnhan/patient123` (BENH_NHAN).

## 2. Quy tắc Git bắt buộc

- Nhánh làm việc: **hospital**. `main` là bản gốc — **TUYỆT ĐỐI KHÔNG push, merge hay commit lên main**.
- Push chỉ: `git push origin hospital` (kiểm tra nhánh bằng `git branch --show-current` trước khi push).
- Không commit: `backend/data/` (H2 runtime), `frontend/node_modules/`, `frontend/.next/`, `.env`, `.env.local` (đã có trong `.gitignore`).

## 3. Workflow mỗi phiên làm việc

1. `git -C . branch --show-current` → phải là **hospital**.
2. Chọn đúng **1 việc nhỏ**, làm xong chạy kiểm tra (mục 7) rồi mới commit.
3. Đụng gì tra nấy: phạm vi → `README.md`; đặt file → mục 4; UI → `components/ui.tsx` + trang có sẵn; API → controller tương ứng.
4. Không xóa tính năng có sẵn (khám bệnh, nhập viện, xét nghiệm, kê đơn, thống kê, giường bệnh…) khi thêm cái mới.

## 4. Bản đồ đặt file

| Muốn thêm | Backend (`com.benhvien`) | Frontend (`frontend/src`) |
|---|---|---|
| Thực thể mới | `domain/` (+ `repository/` nếu mới) — schema chỉ đổi qua entity | — |
| API mới | `web/XxxController.java` + record `web/dto/XxxReq.java` | gọi qua `lib/api.ts` |
| Tìm kiếm/bản đồ | `web/TimKiemController.java` (gần nhất theo bệnh lý, nhân sự BV) | `app/ban-do/page.tsx` (Leaflet) |
| Nghiệp vụ auth | `auth/` (JWT, Google) + `web/AuthController.java` | `lib/auth.tsx` |
| Cấu hình Spring | `config/` (SecurityConfig, WebConfig) | — |
| Dữ liệu mẫu | `seed/DataSeeder.java` | — |
| Trang mới | — | `app/<ten-kebab>/page.tsx` (`'use client'`) |
| Component dùng chung | — | `components/` (`CrudPage`, `ui`, `Sidebar`, `GoogleButton`) |

**Quy ước trang CRUD**: dùng component generic `CrudPage` + `FieldDef/ColumnDef` (xem `app/benh-nhan/page.tsx` làm mẫu) — KHÔNG viết bảng/form tay khi CrudPage đủ dùng.

## 5. Hằng số hệ thống — KHÔNG được phá

1. **Không có envelope `ApiResponse`**: controller trả thẳng entity/DTO, lỗi tiếng Việt qua `GlobalExceptionHandler` (`{"message": "..."}`) — frontend `handle()` trong `lib/api.ts` đọc đúng định dạng này.
2. Controller dùng repository trực tiếp (không có tầng service) — giữ nguyên phong cách.
3. DTO request = Java **record** tên `XxxReq` trong `web/dto`.
4. Tên field/UI tiếng Việt không dấu (entity `BenhNhan`, bảng `nguoi_dung`, label "Bệnh nhân"…).
5. Tailwind 3 qua `tailwind.config.ts` (màu `brand`); KHÔNG nâng Tailwind 4, KHÔNG thêm Tailwind config khác.
6. Icon: inline SVG (đường path có sẵn trong `Sidebar`, `page.tsx`) — không thêm thư viện icon.
7. TypeScript strict, không `any` (dùng `unknown` + type-narrow).

## 6. Luồng auth (nhánh hospital)

1. `POST /api/auth/register` / `POST /api/auth/login` / `POST /api/auth/google` → `AuthResponse { token, nguoiDung }` (BCrypt cho mật khẩu, JWT HS256 hết hạn 24h).
2. `GET /api/auth/me` (cần Bearer) → `NguoiDungView` (không trả mật khẩu).
3. Mọi `/api/**` khác **bắt buộc Bearer token** (SecurityConfig, trả 401 `{"message": "Chưa đăng nhập..."}`).
4. Frontend: `lib/api.ts` tự gắn `Authorization: Bearer` từ localStorage; `AuthProvider` (`lib/auth.tsx`) khôi phục phiên, chuyển hướng về `/dang-nhap` nếu chưa đăng nhập; Sidebar hiện user + nút đăng xuất.
5. Google: frontend dùng GIS script (nút trong `components/GoogleButton.tsx`) → gửi `idToken` về `/api/auth/google` → backend xác minh qua `https://oauth2.googleapis.com/tokeninfo` (check `aud`, `email_verified`, `exp`) → tìm/tạo `NguoiDung` (vai trò BENH_NHAN, username = email).
6. Bật Google cần **cả 2 phía**: `NEXT_PUBLIC_GOOGLE_CLIENT_ID` (frontend, xem `frontend/.env.example`) và `GOOGLE_CLIENT_ID` (backend env, xem `application.yml`). Thiếu thì tính năng Google tắt một cách êm (hiện gợi ý), phần còn lại vẫn chạy.

## 7. Lệnh chạy & kiểm tra trước khi báo xong

```bash
# Cách nhanh nhất (Windows): chạy start-all.bat — hoặc từng phần:
cd backend  && mvnw.cmd spring-boot:run        # :8081, H2 file tự tạo
cd frontend && npm install && npm run dev      # :3000, proxy /api

# Kiểm tra bắt buộc
cd backend  && mvnw.cmd compile                # sạch lỗi
cd frontend && npm run build                   # sạch lỗi

# Test auth nhanh (curl) — backend đang chạy:
curl -X POST localhost:8081/api/auth/login -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
curl localhost:8081/api/thong-ke               # 401 khi chưa có token, 200 khi gắn Bearer
```

**Definition of Done**: backend compile sạch + frontend build sạch + chức năng đi được end-to-end qua UI + lỗi hiển thị tiếng Việt + không xóa tính năng có sẵn của main.

## 8. Khi gặp lỗi

1. Đọc đúng stack trace, sửa root cause — không tắt security/validation để "cho qua".
2. Sửa xong chạy lại chứng minh hết lỗi; không giải quyết được thì ghi lại trong issue/commit message, không giấu.
3. Mất mạng khi Maven tải dependency → chạy lại `mvnw compile` (retry, không đổi pom tùy tiện).
