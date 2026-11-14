package com.benhvien.web;

import com.benhvien.domain.BacSi;
import com.benhvien.domain.BenhVien;
import com.benhvien.domain.DanhGia;
import com.benhvien.domain.DangKyKham;
import com.benhvien.domain.Khoa;
import com.benhvien.domain.NguoiDung;
import com.benhvien.domain.TrangThaiDangKy;
import com.benhvien.domain.VaiTro;
import com.benhvien.domain.YTa;
import com.benhvien.repository.BacSiRepository;
import com.benhvien.repository.BenhVienRepository;
import com.benhvien.repository.DangKyKhamRepository;
import com.benhvien.repository.DanhGiaRepository;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.repository.YTaRepository;
import com.benhvien.web.dto.BacSiView;
import com.benhvien.web.dto.BenhVienGanRes;
import com.benhvien.web.dto.DangKyKhamReq;
import com.benhvien.web.dto.DangKyKhamView;
import com.benhvien.web.dto.DanhGiaReq;
import com.benhvien.web.dto.DanhGiaRes;
import com.benhvien.web.dto.DanhGiaView;
import com.benhvien.web.dto.KhoaPhuHopRes;
import com.benhvien.web.dto.NhanSuRes;
import com.benhvien.web.dto.YTaView;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Tìm bệnh viện gần nhất trên bản đồ Việt Nam theo bệnh lý người bệnh gặp phải:
 * ảnh bệnh viện, trạng thái mở/đóng cửa, thời gian di chuyển đường bộ thật (OSRM),
 * nhân sự và đánh giá (review) của từng bệnh viện.
 */
@RestController
@RequestMapping("/api/tim-kiem")
public class TimKiemController {

    /** Dịch vụ định tuyến công khai của OSRM (không cần API key) — tính thời gian đi đường bộ thật. */
    private static final String OSRM_URL = "https://router.project-osrm.org/route/v1/driving/";

    private final BenhVienRepository benhVienRepo;
    private final KhoaRepository khoaRepo;
    private final BacSiRepository bacSiRepo;
    private final YTaRepository yTaRepo;
    private final DanhGiaRepository danhGiaRepo;
    private final DangKyKhamRepository dangKyKhamRepo;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public TimKiemController(BenhVienRepository benhVienRepo, KhoaRepository khoaRepo,
                             BacSiRepository bacSiRepo, YTaRepository yTaRepo,
                             DanhGiaRepository danhGiaRepo, DangKyKhamRepository dangKyKhamRepo) {
        this.benhVienRepo = benhVienRepo;
        this.khoaRepo = khoaRepo;
        this.bacSiRepo = bacSiRepo;
        this.yTaRepo = yTaRepo;
        this.danhGiaRepo = danhGiaRepo;
        this.dangKyKhamRepo = dangKyKhamRepo;
    }

    /** Bệnh lý phổ biến → từ khóa chuyên môn (so khớp với tên/mô tả khoa và chuyên môn bác sĩ). */
    private static final Map<String, List<String>> BENH_LY_THEO_CHUYEN_MON = new LinkedHashMap<>();

    static {
        BENH_LY_THEO_CHUYEN_MON.put("Tim mạch", List.of("tim", "huyet ap", "mach", "nhoi mau", "lo nhip", "dau nguc"));
        BENH_LY_THEO_CHUYEN_MON.put("Tiêu hóa", List.of("da day", "vi day", "tieu hoa", "ruot", "gan", "viem gan", "tao bon", "di ung"));
        BENH_LY_THEO_CHUYEN_MON.put("Nội tiết & Tiểu đường", List.of("tieu duong", "duong huyet", "noi tiet", "tuyen giap", "giap"));
        BENH_LY_THEO_CHUYEN_MON.put("Chấn thương & Xương khớp", List.of("gay xuong", "chan thuong", "xuong khop", "khop", "cot song", "dau lung", "thoat vi"));
        BENH_LY_THEO_CHUYEN_MON.put("Ngoại khoa & Cấp cứu", List.of("ngoai khoa", "phau thuat", "sat thuong", "do ma", "cap cuu"));
        BENH_LY_THEO_CHUYEN_MON.put("Sản phụ khoa", List.of("thai", "don thai", "san phu", "khoa san"));
        BENH_LY_THEO_CHUYEN_MON.put("Nhi khoa", List.of("tre em", "tre nho", "so sinh", "nhi"));
        BENH_LY_THEO_CHUYEN_MON.put("Hô hấp", List.of("ho hap", "hen suyen", "phoi", "viem phoi", "ho"));
        BENH_LY_THEO_CHUYEN_MON.put("Thần kinh", List.of("than kinh", "dau dau", "dau nua dau", "dong kinh", "mat tri nho"));
        BENH_LY_THEO_CHUYEN_MON.put("Tai mũi họng", List.of("tai", "mui", "hong"));
        BENH_LY_THEO_CHUYEN_MON.put("Mắt", List.of("thi luc", "lo mat", "mat"));
        BENH_LY_THEO_CHUYEN_MON.put("Răng hàm mặt", List.of("rang", "ham mat"));
        BENH_LY_THEO_CHUYEN_MON.put("Da liễu", List.of("da lieu", "viem da", "mun"));
        BENH_LY_THEO_CHUYEN_MON.put("Thận & Tiết niệu", List.of("than", "tiet nieu"));
    }

    /** Danh sách nhóm bệnh lý gợi ý cho ô tìm kiếm. */
    @GetMapping("/benh-ly")
    public List<String> benhLyGoiY() {
        return List.copyOf(BENH_LY_THEO_CHUYEN_MON.keySet());
    }

    /** Bảng màu gradient cho ảnh minh họa bệnh viện (chọn theo id). */
    private static final String[][] MAU_ANH = {
            {"#0ea5e9", "#075985"}, {"#10b981", "#047857"}, {"#8b5cf6", "#5b21b6"},
            {"#f59e0b", "#b45309"}, {"#ef4444", "#991b1b"}, {"#06b6d4", "#0e7490"},
    };

    /**
     * Sinh ảnh minh họa SVG cho bệnh viện (tên dạng benhvien{id}-{stt}.svg).
     * Không phụ thuộc dịch vụ ảnh ngoài — ảnh luôn hiển thị được, offline cũng OK.
     */
    @GetMapping(value = "/anh/{ten}.svg", produces = "image/svg+xml")
    public String anhBenhVien(@PathVariable String ten) {
        long id = 0;
        int stt = 1;
        try {
            String[] phan = ten.replace("benhvien", "").split("-");
            id = Long.parseLong(phan[0]);
            if (phan.length > 1) {
                stt = Integer.parseInt(phan[1]);
            }
        } catch (Exception ignore) {
            // ten lạ → dùng màu mặc định
        }
        String[] mau = MAU_ANH[(int) Math.abs(id % MAU_ANH.length)];
        return """
                <svg xmlns='http://www.w3.org/2000/svg' width='640' height='400' viewBox='0 0 640 400'>
                  <defs>
                    <linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>
                      <stop offset='0' stop-color='%s'/>
                      <stop offset='1' stop-color='%s'/>
                    </linearGradient>
                  </defs>
                  <rect width='640' height='400' fill='url(#g)'/>
                  <circle cx='560' cy='70' r='120' fill='rgba(255,255,255,.08)'/>
                  <circle cx='60' cy='350' r='90' fill='rgba(255,255,255,.07)'/>
                  <circle cx='320' cy='168' r='60' fill='rgba(255,255,255,.25)'/>
                  <rect x='296' y='136' width='48' height='64' rx='8' fill='#ffffff'/>
                  <rect x='288' y='144' width='64' height='48' rx='8' fill='#ffffff'/>
                  <text x='320' y='286' text-anchor='middle' font-family='Segoe UI, Arial, sans-serif' font-size='28' font-weight='700' fill='#ffffff'>BỆNH VIỆN</text>
                  <text x='320' y='322' text-anchor='middle' font-family='Segoe UI, Arial, sans-serif' font-size='17' fill='rgba(255,255,255,.85)'>Ảnh minh họa số %d</text>
                </svg>
                """.formatted(mau[0], mau[1], stt);
    }

    /**
     * Gợi ý bệnh viện gần nhất từ vị trí của bệnh nhân, ưu tiên bệnh viện có khoa
     * phù hợp với bệnh lý đã nhập. Trả về kèm ảnh, trạng thái mở/đóng cửa,
     * quãng đường và thời gian di chuyển đường bộ (OSRM — sai số thực tế như bản đồ).
     */
    @GetMapping("/benh-vien-gan")
    public List<BenhVienGanRes> benhVienGan(
            @RequestParam(required = false) Double viDo,
            @RequestParam(required = false) Double kinhDo,
            @RequestParam(required = false) String benhLy) {

        String tuKhoa = chuanHoa(benhLy);
        List<String> monCan = monPhuHop(tuKhoa);
        boolean coViTri = viDo != null && kinhDo != null;

        // Tải trước điểm đánh giá của mọi bệnh viện (1 câu truy vấn duy nhất)
        Map<Long, List<DanhGia>> danhGiaTheoBv = new HashMap<>();
        for (DanhGia dg : danhGiaRepo.findAll()) {
            danhGiaTheoBv.computeIfAbsent(dg.getBenhVien().getId(), k -> new ArrayList<>()).add(dg);
        }

        // Thời gian đi đường bộ thật (OSRM) — gọi song song, quá hạn thì dùng ước lượng
        Map<Long, double[]> duongBoThucTe = new HashMap<>(); // id -> {quangDuongKm, thoiGianPhut}
        if (coViTri) {
            List<CompletableFuture<Void>> cacNhiemVu = new ArrayList<>();
            for (BenhVien bv : benhVienRepo.findAll()) {
                if (bv.getViDo() == null || bv.getKinhDo() == null) continue;
                CompletableFuture<double[]> nhiemVu = duongBoThucTe(viDo, kinhDo, bv.getViDo(), bv.getKinhDo())
                        .orTimeout(5, TimeUnit.SECONDS)
                        .exceptionally(ex -> null);
                cacNhiemVu.add(nhiemVu.thenAccept(kq -> {
                    if (kq != null) duongBoThucTe.put(bv.getId(), kq);
                }));
            }
            try {
                CompletableFuture.allOf(cacNhiemVu.toArray(new CompletableFuture[0]))
                        .get(7, TimeUnit.SECONDS);
            } catch (Exception ex) {
                // OSRM chậm/lỗi → dùng ước lượng cho các bệnh viện chưa có kết quả
            }
        }

        LocalTime bayGio = LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<BenhVienGanRes> ketQua = new ArrayList<>();

        for (BenhVien bv : benhVienRepo.findAll()) {
            List<Khoa> cacKhoa = khoaRepo.findByBenhVienIdOrderById(bv.getId());
            int soBacSi = 0;
            int soYTa = 0;
            List<KhoaPhuHopRes> phuHop = new ArrayList<>();

            for (Khoa k : cacKhoa) {
                List<BacSi> bs = bacSiRepo.findByKhoaIdOrderById(k.getId());
                List<YTa> yt = yTaRepo.findByKhoaIdOrderById(k.getId());
                soBacSi += bs.size();
                soYTa += yt.size();
                if (!monCan.isEmpty() && khoaPhuHop(k, bs, monCan, tuKhoa)) {
                    phuHop.add(new KhoaPhuHopRes(k.getId(), k.getTenKhoa(), k.getMoTa(), bs.size()));
                }
            }

            // Có nhập bệnh lý mà bệnh viện không có khoa phù hợp → không đề xuất
            if (!monCan.isEmpty() && phuHop.isEmpty()) {
                continue;
            }

            Double kmThang = coViTri && bv.getViDo() != null && bv.getKinhDo() != null
                    ? khoangCachKm(viDo, kinhDo, bv.getViDo(), bv.getKinhDo())
                    : null;
            Double quangDuongKm = null;
            Double thoiGianPhut = null;
            boolean uocLuong = false;
            if (coViTri && bv.getViDo() != null && bv.getKinhDo() != null) {
                double[] thucTe = duongBoThucTe.get(bv.getId());
                if (thucTe != null) {
                    quangDuongKm = thucTe[0];
                    thoiGianPhut = thucTe[1];
                } else {
                    // Ước lượng: đường bộ ~1.3 lần đường thẳng, xe máy trung bình 30 km/h
                    uocLuong = true;
                    quangDuongKm = round1(kmThang * 1.3);
                    thoiGianPhut = round1(quangDuongKm / 30.0 * 60.0);
                }
            }

            List<DanhGia> dgCuaBv = danhGiaTheoBv.getOrDefault(bv.getId(), List.of());
            Double trungBinh = dgCuaBv.isEmpty() ? null
                    : round1(dgCuaBv.stream().mapToInt(DanhGia::getSoSao).average().orElse(0));

            ketQua.add(new BenhVienGanRes(
                    bv.getId(), bv.getTen(), bv.getDiaChi(), bv.getSoDienThoai(), bv.getEmail(),
                    bv.getViDo(), bv.getKinhDo(),
                    tachAnh(bv.getAnhChinh()),
                    bv.getGioMoCua() == null ? null : bv.getGioMoCua().toString(),
                    bv.getGioDongCua() == null ? null : bv.getGioDongCua().toString(),
                    dangMoCua(bv, bayGio),
                    kmThang, quangDuongKm, thoiGianPhut, uocLuong,
                    phuHop, trungBinh, dgCuaBv.size(),
                    cacKhoa.size(), soBacSi, soYTa));
        }

        ketQua.sort(Comparator.comparing(BenhVienGanRes::khoangCachKm,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return ketQua;
    }

    /** Nhân sự (bác sĩ + y tá) của một bệnh viện — hiện khi chọn bệnh viện trên bản đồ. */
    @GetMapping("/benh-vien/{id}/nhan-su")
    public NhanSuRes nhanSu(@PathVariable Long id) {
        BenhVien bv = benhVienRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bệnh viện " + id));
        List<BacSiView> bacSi = new ArrayList<>();
        List<YTaView> yTa = new ArrayList<>();
        for (Khoa k : khoaRepo.findByBenhVienIdOrderById(id)) {
            for (BacSi b : bacSiRepo.findByKhoaIdOrderById(k.getId())) {
                bacSi.add(new BacSiView(b.getId(), b.getMaBacSi(), b.getHoTen(), b.getChuyenMon(),
                        b.getHocHam(), b.getSoDienThoai(), b.getEmail(), k.getTenKhoa()));
            }
            for (YTa y : yTaRepo.findByKhoaIdOrderById(k.getId())) {
                yTa.add(new YTaView(y.getId(), y.getMaYTa(), y.getHoTen(), y.getTrinhDo(),
                        y.getSoDienThoai(), y.getEmail(), k.getTenKhoa()));
            }
        }
        return new NhanSuRes(id, bv.getTen(), bacSi, yTa);
    }

    /** Đánh giá (review) của một bệnh viện: điểm trung bình + danh sách nhận xét. */
    @GetMapping("/benh-vien/{id}/danh-gia")
    public DanhGiaRes danhGia(@PathVariable Long id) {
        BenhVien bv = benhVienRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bệnh viện " + id));
        return taoDanhGiaRes(bv);
    }

    /** Viết đánh giá bệnh viện — PHÂN QUYỀN: chỉ vai trò BỆNH NHÂN được viết. */
    @PostMapping("/benh-vien/{id}/danh-gia")
    @Transactional
    public DanhGiaRes vietDanhGia(@PathVariable Long id, @Valid @RequestBody DanhGiaReq r) {
        NguoiDung nguoi = nguoiDungHienTai();
        if (nguoi.getVaiTro() != VaiTro.BENH_NHAN) {
            throw new AccessDeniedException("Chỉ bệnh nhân mới được viết đánh giá bệnh viện.");
        }
        BenhVien bv = benhVienRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bệnh viện " + id));
        DanhGia dg = new DanhGia();
        dg.setBenhVien(bv);
        dg.setNguoiDung(nguoi);
        dg.setSoSao(r.soSao());
        dg.setNoiDung(r.noiDung());
        dg.setThoiGian(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        danhGiaRepo.save(dg);
        return taoDanhGiaRes(bv);
    }

    /** Đăng ký khám bệnh tại bệnh viện — PHÂN QUYỀN: chỉ vai trò BỆNH NHÂN. */
    @PostMapping("/benh-vien/{id}/dang-ky-kham")
    @Transactional
    public DangKyKhamView dangKyKham(@PathVariable Long id, @Valid @RequestBody DangKyKhamReq r) {
        NguoiDung nguoi = nguoiDungHienTai();
        if (nguoi.getVaiTro() != VaiTro.BENH_NHAN) {
            throw new AccessDeniedException("Chỉ bệnh nhân mới được đăng ký khám bệnh.");
        }
        if (r.thoiGianHen().isBefore(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
            throw new IllegalArgumentException("Thời gian hẹn phải sau thời điểm hiện tại.");
        }
        BenhVien bv = benhVienRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bệnh viện " + id));
        DangKyKham dk = new DangKyKham();
        dk.setBenhVien(bv);
        dk.setNguoiDung(nguoi);
        dk.setBenhLy(r.benhLy());
        dk.setThoiGianHen(r.thoiGianHen());
        dk.setGhiChu(r.ghiChu());
        dk.setTrangThai(TrangThaiDangKy.CHO_DUYET);
        dk.setThoiGianDangKy(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        dangKyKhamRepo.save(dk);
        return viewDangKy(dk);
    }

    /** Đăng ký khám của chính tài khoản đang đăng nhập, tại một bệnh viện. */
    @GetMapping("/benh-vien/{id}/dang-ky-cua-toi")
    public List<DangKyKhamView> dangKyCuaToiTaiBenhVien(@PathVariable Long id) {
        NguoiDung nguoi = nguoiDungHienTai();
        return dangKyKhamRepo
                .findByNguoiDungIdAndBenhVienIdOrderByThoiGianDangKyDesc(nguoi.getId(), id)
                .stream().map(this::viewDangKy).toList();
    }

    /** Hủy đăng ký khám của chính mình (chỉ khi còn ở trạng thái chờ duyệt). */
    @PostMapping("/dang-ky/{idDangKy}/huy")
    @Transactional
    public DangKyKhamView huyDangKy(@PathVariable Long idDangKy) {
        NguoiDung nguoi = nguoiDungHienTai();
        DangKyKham dk = dangKyKhamRepo.findById(idDangKy)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đăng ký khám " + idDangKy));
        if (!dk.getNguoiDung().getId().equals(nguoi.getId())) {
            throw new AccessDeniedException("Chỉ được hủy đăng ký khám của chính mình.");
        }
        if (dk.getTrangThai() != TrangThaiDangKy.CHO_DUYET) {
            throw new IllegalArgumentException("Chỉ đăng ký đang chờ duyệt mới có thể hủy.");
        }
        dk.setTrangThai(TrangThaiDangKy.DA_HUY);
        return viewDangKy(dk);
    }

    // ===== Hàm nội bộ =====

    private NguoiDung nguoiDungHienTai() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof NguoiDung nd) {
            return nd;
        }
        throw new AccessDeniedException("Chưa đăng nhập hoặc phiên đã hết hạn.");
    }

    private DanhGiaRes taoDanhGiaRes(BenhVien bv) {
        List<DanhGia> ds = danhGiaRepo.findByBenhVienIdOrderByThoiGianDesc(bv.getId());
        Double trungBinh = ds.isEmpty() ? null
                : round1(ds.stream().mapToInt(DanhGia::getSoSao).average().orElse(0));
        List<DanhGiaView> danhSach = ds.stream()
                .map(dg -> new DanhGiaView(dg.getId(),
                        dg.getNguoiDung() == null ? "Bệnh nhân ẩn danh" : dg.getNguoiDung().getHoTen(),
                        dg.getNguoiDung() == null ? null : dg.getNguoiDung().getVaiTro().name(),
                        dg.getSoSao(), dg.getNoiDung(),
                        dg.getThoiGian() == null ? null : dg.getThoiGian().toString()))
                .toList();
        return new DanhGiaRes(bv.getId(), bv.getTen(), trungBinh, ds.size(), danhSach);
    }

    private DangKyKhamView viewDangKy(DangKyKham dk) {
        return new DangKyKhamView(dk.getId(),
                dk.getBenhVien() == null ? null : dk.getBenhVien().getTen(),
                dk.getBenhLy(), dk.getThoiGianHen() == null ? null : dk.getThoiGianHen().toString(),
                dk.getGhiChu(),
                dk.getTrangThai() == null ? null : dk.getTrangThai().name(),
                dk.getThoiGianDangKy() == null ? null : dk.getThoiGianDangKy().toString());
    }

    /** Trạng thái mở cửa theo giờ hiện tại (mở qua đêm nếu giờ đóng < giờ mở). */
    private boolean dangMoCua(BenhVien bv, LocalTime bayGio) {
        if (bv.getGioMoCua() == null || bv.getGioDongCua() == null) {
            return true; // không khai báo giờ → coi như luôn mở (cấp cứu)
        }
        if (bv.getGioMoCua().equals(bv.getGioDongCua())) {
            return true; // 24/24
        }
        if (bv.getGioMoCua().isBefore(bv.getGioDongCua())) {
            return !bayGio.isBefore(bv.getGioMoCua()) && bayGio.isBefore(bv.getGioDongCua());
        }
        return !bayGio.isBefore(bv.getGioMoCua()) || bayGio.isBefore(bv.getGioDongCua());
    }

    private List<String> tachAnh(String anhChinh) {
        if (anhChinh == null || anhChinh.isBlank()) {
            return List.of();
        }
        return List.of(anhChinh.split("\\|"));
    }

    /**
     * Gọi OSRM lấy quãng đường + thời gian đi đường bộ thật (phút, xe máy/ô tô).
     * Trả về null nếu dịch vụ không phản hồi (caller sẽ dùng ước lượng).
     */
    private CompletableFuture<double[]> duongBoThucTe(double lat1, double lon1, double lat2, double lon2) {
        String url = OSRM_URL + lon1 + "," + lat1 + ";" + lon2 + "," + lat2 + "?overview=false";
        HttpRequest yeuCau = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();
        return http.sendAsync(yeuCau, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    if (res.statusCode() != 200) {
                        throw new IllegalStateException("OSRM trả về mã " + res.statusCode());
                    }
                    JsonNode n = mapper.readTree(res.body());
                    JsonNode tuyen = n.path("routes").path(0);
                    if (tuyen.isMissingNode()) {
                        throw new IllegalStateException("OSRM không có tuyến đường");
                    }
                    double km = tuyen.path("distance").asDouble() / 1000.0;
                    double phut = tuyen.path("duration").asDouble() / 60.0;
                    return new double[]{round1(km), round1(phut)};
                });
    }

    /** Bỏ dấu, về chữ thường, bỏ ký tự lạ — để so khớp tiếng Việt dễ dàng. */
    private String chuanHoa(String s) {
        if (s == null) {
            return "";
        }
        String khongDau = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return khongDau.toLowerCase().replaceAll("[^a-z0-9 ]+", " ").replaceAll("\\s+", " ").trim();
    }

    /** Gom các nhóm chuyên môn phù hợp với bệnh lý người dùng nhập. */
    private List<String> monPhuHop(String tuKhoa) {
        List<String> ketQua = new ArrayList<>();
        if (tuKhoa.isEmpty()) {
            return ketQua;
        }
        for (Map.Entry<String, List<String>> e : BENH_LY_THEO_CHUYEN_MON.entrySet()) {
            String tenNhom = chuanHoa(e.getKey());
            boolean trung = tuKhoa.contains(tenNhom) || tenNhom.contains(tuKhoa)
                    || e.getValue().stream().anyMatch(tuKhoa::contains);
            if (trung) {
                ketQua.add(tenNhom);
                ketQua.addAll(e.getValue());
            }
        }
        return ketQua;
    }

    /** Khoa phù hợp khi tên/mô tả khoa hoặc chuyên môn bác sĩ chứa từ khóa bệnh lý. */
    private boolean khoaPhuHop(Khoa k, List<BacSi> bacSiCuaKhoa, List<String> monCan, String tuKhoa) {
        StringBuilder text = new StringBuilder(chuanHoa(k.getTenKhoa() + " " + k.getMoTa()));
        for (BacSi b : bacSiCuaKhoa) {
            text.append(' ').append(chuanHoa(b.getChuyenMon()));
        }
        String sanSang = text.toString().trim();
        if (sanSang.isEmpty()) {
            return false;
        }
        return sanSang.contains(tuKhoa) || monCan.stream().anyMatch(sanSang::contains);
    }

    /** Khoảng cách Haversine giữa vị trí bệnh nhân và bệnh viện (km). */
    private double khoangCachKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }
}
