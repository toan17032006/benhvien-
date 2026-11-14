package com.benhvien.web;

import com.benhvien.domain.BacSi;
import com.benhvien.domain.BenhVien;
import com.benhvien.domain.Khoa;
import com.benhvien.domain.YTa;
import com.benhvien.repository.BacSiRepository;
import com.benhvien.repository.BenhVienRepository;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.repository.YTaRepository;
import com.benhvien.web.dto.BacSiView;
import com.benhvien.web.dto.BenhVienGanRes;
import com.benhvien.web.dto.KhoaPhuHopRes;
import com.benhvien.web.dto.NhanSuRes;
import com.benhvien.web.dto.YTaView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Tìm bệnh viện gần nhất trên bản đồ Việt Nam theo bệnh lý người bệnh gặp phải,
 * kèm thông tin nhân sự (bác sĩ, y tá) của từng bệnh viện.
 */
@RestController
@RequestMapping("/api/tim-kiem")
public class TimKiemController {

    private final BenhVienRepository benhVienRepo;
    private final KhoaRepository khoaRepo;
    private final BacSiRepository bacSiRepo;
    private final YTaRepository yTaRepo;

    public TimKiemController(BenhVienRepository benhVienRepo, KhoaRepository khoaRepo,
                             BacSiRepository bacSiRepo, YTaRepository yTaRepo) {
        this.benhVienRepo = benhVienRepo;
        this.khoaRepo = khoaRepo;
        this.bacSiRepo = bacSiRepo;
        this.yTaRepo = yTaRepo;
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

    /**
     * Gợi ý bệnh viện gần nhất từ vị trí của bệnh nhân, ưu tiên bệnh viện có khoa
     * phù hợp với bệnh lý đã nhập. Không truyền vị trí/bệnh lý → trả về toàn bộ bệnh viện.
     */
    @GetMapping("/benh-vien-gan")
    public List<BenhVienGanRes> benhVienGan(
            @RequestParam(required = false) Double viDo,
            @RequestParam(required = false) Double kinhDo,
            @RequestParam(required = false) String benhLy) {

        String tuKhoa = chuanHoa(benhLy);
        List<String> monCan = monPhuHop(tuKhoa);
        boolean coViTri = viDo != null && kinhDo != null;

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

            Double km = coViTri && bv.getViDo() != null && bv.getKinhDo() != null
                    ? khoangCachKm(viDo, kinhDo, bv.getViDo(), bv.getKinhDo())
                    : null;
            ketQua.add(new BenhVienGanRes(bv.getId(), bv.getTen(), bv.getDiaChi(), bv.getSoDienThoai(),
                    bv.getEmail(), bv.getViDo(), bv.getKinhDo(), km, phuHop,
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

    // ===== Hàm nội bộ =====

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
}
