package com.benhvien.web;

import com.benhvien.domain.TrangThaiGiuong;
import com.benhvien.domain.TrangThaiKham;
import com.benhvien.domain.TrangThaiNhapVien;
import com.benhvien.domain.TrangThaiXetNghiem;
import com.benhvien.repository.BacSiRepository;
import com.benhvien.repository.BenhNhanRepository;
import com.benhvien.repository.DonThuocRepository;
import com.benhvien.repository.GiuongBenhRepository;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.repository.LanKhamBenhRepository;
import com.benhvien.repository.LanNhapVienRepository;
import com.benhvien.repository.LichLamViecRepository;
import com.benhvien.repository.XetNghiemRepository;
import com.benhvien.repository.YTaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Số liệu tổng hợp cho trang tổng quan của frontend. */
@RestController
@RequestMapping("/api/thong-ke")
public class ThongKeController {

    private final BenhNhanRepository benhNhanRepo;
    private final BacSiRepository bacSiRepo;
    private final YTaRepository yTaRepo;
    private final KhoaRepository khoaRepo;
    private final GiuongBenhRepository giuongRepo;
    private final LanKhamBenhRepository lanKhamRepo;
    private final LanNhapVienRepository lanNhapVienRepo;
    private final XetNghiemRepository xetNghiemRepo;
    private final DonThuocRepository donThuocRepo;
    private final LichLamViecRepository lichLamViecRepo;

    public ThongKeController(BenhNhanRepository benhNhanRepo, BacSiRepository bacSiRepo,
                             YTaRepository yTaRepo, KhoaRepository khoaRepo, GiuongBenhRepository giuongRepo,
                             LanKhamBenhRepository lanKhamRepo, LanNhapVienRepository lanNhapVienRepo,
                             XetNghiemRepository xetNghiemRepo, DonThuocRepository donThuocRepo,
                             LichLamViecRepository lichLamViecRepo) {
        this.benhNhanRepo = benhNhanRepo;
        this.bacSiRepo = bacSiRepo;
        this.yTaRepo = yTaRepo;
        this.khoaRepo = khoaRepo;
        this.giuongRepo = giuongRepo;
        this.lanKhamRepo = lanKhamRepo;
        this.lanNhapVienRepo = lanNhapVienRepo;
        this.xetNghiemRepo = xetNghiemRepo;
        this.donThuocRepo = donThuocRepo;
        this.lichLamViecRepo = lichLamViecRepo;
    }

    @GetMapping
    public Map<String, Object> thongKe() {
        LocalDate homNay = LocalDate.now();
        LocalDateTime dauNgay = homNay.atStartOfDay();
        LocalDateTime cuoiNgay = homNay.atTime(23, 59, 59);

        Map<String, Object> res = new HashMap<>();
        res.put("soBenhNhan", benhNhanRepo.count());
        res.put("soBacSi", bacSiRepo.count());
        res.put("soYTa", yTaRepo.count());
        res.put("soKhoa", khoaRepo.count());
        res.put("soGiuong", giuongRepo.count());
        res.put("soGiuongTrong", giuongRepo.findByTrangThaiOrderById(TrangThaiGiuong.TRONG).size());
        res.put("soGiuongDangSuDung", giuongRepo.findByTrangThaiOrderById(TrangThaiGiuong.DANG_SU_DUNG).size());
        res.put("soGiuongBaoTri", giuongRepo.findByTrangThaiOrderById(TrangThaiGiuong.BAO_TRI).size());
        res.put("soLanKhamHomNay", lanKhamRepo.findAll().stream()
                .filter(k -> k.getThoiGianKham() != null
                        && !k.getThoiGianKham().isBefore(dauNgay) && !k.getThoiGianKham().isAfter(cuoiNgay))
                .count());
        res.put("soLanKhamDangKham", lanKhamRepo.findByTrangThaiOrderByThoiGianKhamDesc(TrangThaiKham.DANG_KHAM).size());
        res.put("soDangNhapVien", lanNhapVienRepo.findByTrangThaiOrderByNgayNhapDesc(TrangThaiNhapVien.DANG_DIEU_TRI).size());
        res.put("soXetNghiemChoKetQua", xetNghiemRepo.findByTrangThaiOrderByIdDesc(TrangThaiXetNghiem.CHO_KET_QUA).size());
        res.put("soDonThuoc", donThuocRepo.count());

        List<Map<String, Object>> lichHomNay = lichLamViecRepo.findByNgayOrderByIdDesc(homNay).stream()
                .limit(8)
                .map(l -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", l.getId());
                    m.put("ngay", l.getNgay());
                    m.put("ghiChu", l.getGhiChu());
                    m.put("bacSi", l.getBacSi() == null ? null : l.getBacSi().getHoTen());
                    m.put("chuyenMon", l.getBacSi() == null ? null : l.getBacSi().getChuyenMon());
                    m.put("phongKham", l.getPhongKham() == null ? null : l.getPhongKham().getTenPhong());
                    return m;
                })
                .toList();
        res.put("lichLamViecHomNay", lichHomNay);
        res.put("lanKhamMoiNhat", lanKhamRepo.findAllByOrderByThoiGianKhamDesc().stream().limit(8).toList());
        res.put("nhapVienMoiNhat", lanNhapVienRepo.findAllByOrderByNgayNhapDesc().stream().limit(6).toList());
        return res;
    }
}
