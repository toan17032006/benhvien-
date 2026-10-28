# 🏥 Hệ thống Quản lý Bệnh viện

Ứng dụng quản lý bệnh viện xây dựng **hoàn toàn theo sơ đồ ERD** đã cho — gồm **17 bảng** (15 thực thể + 2 bảng trung gian quan hệ N-N), với đầy đủ nghiệp vụ khám bệnh, nhập viện, kê đơn thuốc, xét nghiệm, phân công y tá và lịch trực.

| Thành phần | Công nghệ | Cổng |
|---|---|---|
| Backend | **Spring Boot 4** (Java 17+), JPA/Hibernate, Validation | `8081` |
| Frontend | **Next.js 14 + TypeScript + Tailwind CSS** | `3000` |
| CSDL mặc định | **H2 file** (tự lưu ở `backend/data/`) — **không cần cài gì** | — |
| CSDL tùy chọn | MySQL (profile `mysql`) | `3306` |

> ✅ **Không cần Docker, không cần cài Maven** (dùng Maven Wrapper sẵn trong dự án).

---

## 🚀 Chạy nhanh

### Yêu cầu máy (cả người nhận code đều cần)

> ⚠️ Làm đúng thứ tự: **Bước 0** cài 2 phần mềm bên dưới → **Bước 1** chạy app. Chỉ cần làm 1 lần trên mỗi máy.

#### Bước 0.1 — Cài JDK 17 hoặc mới hơn (bắt buộc)

**Kiểm tra máy đã có chưa:** mở Command Prompt (nhấn `Win + R` → gõ `cmd` → Enter) và chạy:

```
java -version
```

- Nếu hiện dòng `openjdk version "17.x"` / `"21.x"` / mới hơn → **đã đủ, bỏ qua bước cài**.
- Nếu báo `'java' is not recognized...` hoặc version nhỏ hơn 17 → cài theo hướng dẫn dưới.

**Windows 10/11:**

1. **Cách nhanh nhất** — mở Command Prompt và chạy:
   ```
   winget install EclipseAdoptium.Temurin.21.JDK
   ```
2. **Cách tải tay** (nếu không dùng được winget):
   - Vào **https://adoptium.net** (Eclipse Temurin — miễn phí, khuyên dùng)
   - Nhấn nút **Latest LTS Release** để tải file `.msi` cho Windows x64
   - Chạy file vừa tải. Ở màn hình "Custom Setup", **bấm để hiện các mục con và tích chọn cả `Add to PATH` và `Set JAVA_HOME variable`** → Next đến khi xong
3. **Đóng và mở lại** Command Prompt (bắt buộc, để cập nhật PATH), rồi kiểm tra lại:
   ```
   java -version
   ```

**macOS:** cài Homebrew (https://brew.sh) nếu chưa có, rồi chạy `brew install --cask temurin@21` — hoặc tải file `.pkg` tại https://adoptium.net và cài như app thông thường.

**Linux (Ubuntu/Debian):**
```bash
sudo apt update && sudo apt install openjdk-21-jdk
```

> 💡 Máy có sẵn JDK cũ hơn (8/11) cũng không sao — cài thêm JDK 17+ chạy song song, miễn lệnh `java -version` hiển thị 17 trở lên.

#### Bước 0.2 — Cài Node.js 18 hoặc mới hơn (bắt buộc)

**Kiểm tra máy đã có chưa:** trong Command Prompt chạy:

```
node -v
npm -v
```

- Nếu `node -v` hiện `v18.x` / `v20.x` / `v22.x` hay mới hơn → **đã đủ, bỏ qua bước cài**.
- npm tự đi kèm theo Node, **không cần cài riêng**.

**Windows 10/11:**

1. **Cách nhanh nhất:**
   ```
   winget install OpenJS.NodeJS.LTS
   ```
2. **Cách tải tay:** vào **https://nodejs.org** → tải bản **LTS** (file `.msi`) → mở file, bấm Next liên tục (mặc định đã tự thêm vào PATH) → xong thì **đóng và mở lại** Command Prompt rồi kiểm tra `node -v`.

**macOS:** `brew install node` — hoặc tải file `.pkg` bản LTS tại https://nodejs.org.

**Linux:**
```bash
# Ubuntu/Debian (qua NodeSource)
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install -y nodejs

# hoặc qua nvm (mọi distro)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
# đóng & mở lại terminal rồi:
nvm install --lts
```

#### Lỗi khi cài JDK/Node — cách xử lý

| Hiện tượng | Cách sửa |
|---|---|
| `java` (hoặc `node`) vẫn không được nhận diện sau khi cài | Chưa **đóng và mở lại** Command Prompt/terminal. Nếu vẫn lỗi: phần mềm chưa được thêm vào PATH — cài lại và tích "Add to PATH", hoặc thêm thư mục cài đặt (vd `C:\Program Files\Eclipse Adoptium\jdk-21...bin`) vào biến môi trường **Path** |
| Máy có nhiều JDK, Maven chọn nhầm bản cũ | Đặt biến môi trường **JAVA_HOME** trỏ tới thư mục JDK 17+ (vd `C:\Program Files\Eclipse Adoptium\jdk-21.0.6+7-hotspot`) — tìm "environment variables" trong Start menu để đặt |
| `winget` không tồn tại | Cài "App Installer" từ Microsoft Store, hoặc dùng cách tải tay bên trên |
| Node cũ hơn v18 | Cài đè bằng bản LTS mới từ https://nodejs.org là được |

---

### Bước 1 — Chạy ứng dụng

### Windows — nhấp đúp 1 file duy nhất
```
start-all.bat
```
Sẽ mở 2 cửa sổ (Backend + Frontend). **Lần đầu chạy** mất vài phút để tải dependencies, các lần sau chạy ngay. Sau đó mở trình duyệt:

> ### 🌐 http://localhost:3000

### macOS / Linux
```bash
chmod +x start-all.sh
./start-all.sh
```

### Tắt ứng dụng
- Windows: đóng 2 cửa sổ, hoặc chạy `stop-all.bat`
- macOS/Linux: nhấn `Ctrl+C` trong terminal

### Chạy riêng từng phần (tùy chọn)
```bash
# Backend (cửa sổ 1)
start-backend.bat          # Windows
cd backend && ./mvnw spring-boot:run   # macOS/Linux

# Frontend (cửa sổ 2)
start-frontend.bat         # Windows
cd frontend && npm install && npm run dev   # macOS/Linux
```

---

## 📊 Sơ đồ ERD → các bảng trong CSDL (đúng 100% sơ đồ)

| Thực thể trong ERD | Bảng | Quan hệ chính |
|---|---|---|
| Bệnh Viện | `benh_vien` | 1 bệnh viện **Có** nhiều khoa |
| Khoa | `khoa` | **Có** nhiều giường bệnh, phòng khám, bác sĩ, y tá |
| Giường Bệnh | `giuong_benh` | 1 Lần Nhập Viện **Nằm** trên 1 giường |
| Phòng Khám | `phong_kham` | Lịch làm việc **Có** phòng khám |
| Bác Sĩ | `bac_si` | **Có** lịch làm việc; **Thực hiện** lần khám |
| Y tá | `y_ta` | **Chăm sóc** lần nhập viện (N-N) |
| Ca trực | `ca_truc` | N-N với lịch làm việc |
| Lịch làm việc | `lich_lam_viec` | Bác sĩ + phòng khám + ngày; gắn ca trực |
| Bệnh Nhân | `benh_nhan` | 1 bệnh nhân **Có** nhiều lần khám |
| Lần khám bệnh | `lan_kham_benh` | **Thuộc** lịch làm việc; **Nhập viên** → lần nhập viện |
| Lần Nhập Viện | `lan_nhap_vien` | Nằm trên giường; được y tá chăm sóc |
| Đơn thuốc | `don_thuoc` | 1 lần khám **Có** nhiều đơn |
| Chi tiết Thuốc | `chi_tiet_thuoc` | N-N đơn↔thuốc, mang **Ghi chú thuốc** |
| Thuốc | `thuoc` | Kho thuốc |
| Xét nghiệm | `xet_nghiem` | Thuộc lần khám; **Thực hiện** trên lịch làm việc |
| *(bảng trung gian)* | `cham_soc` | Y tá ↔ Lần nhập viện |
| *(bảng trung gian)* | `lich_ca_truc` | Lịch làm việc ↔ Ca trực |

**Nghiệp vụ tự động theo ERD:**
- Nhập viện từ một lần khám → giường chuyển **Đang sử dụng**, chặn chọn giường đã có người nằm.
- Xuất viện → ghi ngày xuất, **giường tự trống**.
- Xóa lần nhập viện / lịch làm việc → tự tháo tham chiếu an toàn, chặn xóa sai quy trình.

---

## 🧭 Các trang giao diện

- **Bảng điều khiển** `/` — thống kê trực quan, lịch trực hôm nay, bệnh nhân nội trú, lần khám gần đây
- **Lần khám bệnh** `/kham-benh` — danh sách + **trang chi tiết** `/kham-benh/[id]`: sửa chẩn đoán, kê đơn thuốc (thêm/sửa từng thuốc + liều dùng + ghi chú), quản lý xét nghiệm, **nút Nhập viện**
- **Lần nhập viện** `/nhap-vien` — danh sách + **trang chi tiết** `/nhap-vien/[id]`: phân công y tá chăm sóc, xuất viện 1 nút, lần khám/đơn thuốc trong thời gian điều trị
- **Danh mục CRUD** — Bệnh nhân, Bác sĩ, Y tá, Khoa, Phòng khám, Giường bệnh, Ca trực, Lịch làm việc (chọn nhiều ca trực), Thuốc, Đơn thuốc, Xét nghiệm, Bệnh viện

---

## 🔌 API chính (chuẩn REST, trả JSON)

```
GET/POST        /api/{tên-bảng}              # ví dụ: /api/benh-nhan, /api/khoa...
GET/PUT/DELETE  /api/{tên-bảng}/{id}
GET             /api/lan-kham-benh/{id}/nhap-vien    → POST nhập viện từ lần khám
POST            /api/lan-nhap-vien/{id}/xuat-vien    → xuất viện, giải phóng giường
GET             /api/lan-nhap-vien/{id}/y-ta         → y tá chăm sóc
GET             /api/lich-lam-viec/{id}/ca-truc      → ca trực của lịch
GET             /api/thong-ke                        → số liệu bảng điều khiển
```

Giao diện gọi API qua **proxy của Next.js** (`/api/**` → `http://localhost:8081`) nên không gặp lỗi CORS. Nếu đổi cổng backend, đặt biến môi trường `API_URL` trước khi chạy frontend.

- Xem API thử nghiệm: `http://localhost:8081/api/thong-ke`
- Console H2: `http://localhost:8081/h2-console` (JDBC URL `jdbc:h2:file:./data/benhvien`, user `sa`, mật khẩu trống)

---

## 🗄️ Chuyển sang MySQL (tùy chọn, khi triển khai thật)

1. Cài MySQL, tạo schema `benhvien` (hoặc để tự tạo nhờ `createDatabaseIfNotExist=true`)
2. Chạy backend với profile `mysql`:
   ```bash
   cd backend
   mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
   ```
3. Có thể ghi đè kết nối bằng biến môi trường: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

```bash
# ví dụ Windows (PowerShell)
$env:DB_USERNAME="root"; $env:DB_PASSWORD="123456"
mvnw spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

---

## 📁 Cấu trúc dự án

```
benhvien/
├── start-all.bat / start-all.sh   # khởi động cả 2
├── stop-all.bat                   # tắt cả 2 (Windows)
├── backend/                       # Spring Boot
│   └── src/main/java/com/benhvien/
│       ├── domain/                # 17 entity JPA theo ERD
│       ├── repository/            # 17 repository Spring Data
│       ├── web/                   # REST controllers + xử lý lỗi tiếng Việt
│       ├── web/dto/               # DTO có validation
│       ├── config/                # CORS
│       └── seed/                  # dữ liệu mẫu tiếng Việt (tự tạo lần đầu)
└── frontend/                      # Next.js 14 + TS + Tailwind
    └── src/
        ├── app/                   # 15 trang (App Router)
        ├── components/            # CrudPage dùng chung, Sidebar, UI
        └── lib/                   # api client + định dạng ngày/tiền tiếng Việt
```

## ❓ Xử lý sự cố

| Hiện tượng | Cách xử lý |
|---|---|
| Cổng 8081/3000 bị chiếm | Chạy `stop-all.bat`, hoặc đổi cổng backend (`SERVER_PORT=8082` trước khi chạy) và đặt `API_URL=http://localhost:8082` cho frontend |
| Lần đầu chạy chậm | Maven/npm đang tải dependencies — chỉ xảy ra 1 lần |
| CSDL bị sạch mỗi lần xóa thư mục `backend/data/` | H2 lưu theo file; xóa file là reset về dữ liệu mẫu |
| Muốn dữ liệu mẫu sinh lại từ đầu | Xóa thư mục `backend/data/` rồi khởi động lại backend |
