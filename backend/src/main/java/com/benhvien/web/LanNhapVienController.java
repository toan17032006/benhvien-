package com.benhvien.web;

import com.benhvien.domain.ChamSoc;
import com.benhvien.domain.GiuongBenh;
import com.benhvien.domain.LanKhamBenh;
import com.benhvien.domain.LanNhapVien;
import com.benhvien.domain.TrangThaiGiuong;
import com.benhvien.domain.TrangThaiNhapVien;
import com.benhvien.repository.ChamSocRepository;
import com.benhvien.repository.GiuongBenhRepository;
import com.benhvien.repository.LanKhamBenhRepository;
import com.benhvien.repository.LanNhapVienRepository;
import com.benhvien.repository.YTaRepository;
import com.benhvien.web.dto.LanNhapVienReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lan-nhap-vien")
public class LanNhapVienController {

    private final LanNhapVienRepository repo;
    private final GiuongBenhRepository giuongBenhRepo;
    private final YTaRepository yTaRepo;
    private final ChamSocRepository chamSocRepo;
    private final LanKhamBenhRepository lanKhamRepo;

    public LanNhapVienController(LanNhapVienRepository repo, GiuongBenhRepository giuongBenhRepo,
                                 YTaRepository yTaRepo, ChamSocRepository chamSocRepo,
                                 LanKhamBenhRepository lanKhamRepo) {
        this.repo = repo;
        this.giuongBenhRepo = giuongBenhRepo;
        this.yTaRepo = yTaRepo;
        this.chamSocRepo = chamSocRepo;
        this.lanKhamRepo = lanKhamRepo;
    }

    @GetMapping
    public List<LanNhapVien> list(@RequestParam(required = false) TrangThaiNhapVien trangThai,
                                  @RequestParam(required = false) Long giuongBenhId) {
        if (trangThai != null) {
            return repo.findByTrangThaiOrderByNgayNhapDesc(trangThai);
        }
        if (giuongBenhId != null) {
            return repo.findByGiuongBenhIdOrderByNgayNhapDesc(giuongBenhId);
        }
        return repo.findAllByOrderByNgayNhapDesc();
    }

    @GetMapping("/{id}")
    public LanNhapVien get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy lần nhập viện " + id));
    }

    /** Danh sách y tá chăm sóc lần nhập viện này (quan hệ N-N "Chăm sóc"). */
    @GetMapping("/{id}/y-ta")
    public List<ChamSoc> yTaChamSoc(@PathVariable Long id) {
        return chamSocRepo.findByLanNhapVienIdOrderByIdAsc(get(id).getId());
    }

    @PostMapping
    @Transactional
    public LanNhapVien create(@Valid @RequestBody LanNhapVienReq r) {
        LanNhapVien e = new LanNhapVien();
        apply(e, r);
        if (e.getNgayNhap() == null) {
            e.setNgayNhap(LocalDateTime.now());
        }
        e = repo.save(e);
        if (e.getMaNhapVien() == null || e.getMaNhapVien().isBlank()) {
            e.setMaNhapVien("NV-" + String.format("%05d", e.getId()));
        }
        e = repo.save(e);
        replaceYTa(e, r.yTaIds());
        return e;
    }

    @PutMapping("/{id}")
    @Transactional
    public LanNhapVien update(@PathVariable Long id, @Valid @RequestBody LanNhapVienReq r) {
        LanNhapVien e = get(id);
        if (e.getTrangThai() == TrangThaiNhapVien.DA_XUAT_VIEN) {
            throw new IllegalArgumentException("Lần nhập viện đã xuất viện, không thể sửa.");
        }
        apply(e, r);
        e = repo.save(e);
        replaceYTa(e, r.yTaIds());
        return e;
    }

    /** Xuất viện: ghi ngày xuất, giải phóng giường bệnh. */
    @PostMapping("/{id}/xuat-vien")
    @Transactional
    public LanNhapVien xuatVien(@PathVariable Long id) {
        LanNhapVien e = get(id);
        if (e.getTrangThai() == TrangThaiNhapVien.DA_XUAT_VIEN) {
            throw new IllegalArgumentException("Lần nhập viện này đã xuất viện rồi.");
        }
        e.setNgayXuat(LocalDateTime.now());
        e.setTrangThai(TrangThaiNhapVien.DA_XUAT_VIEN);
        GiuongBenh giuong = e.getGiuongBenh();
        if (giuong != null && giuong.getTrangThai() == TrangThaiGiuong.DANG_SU_DUNG) {
            giuong.setTrangThai(TrangThaiGiuong.TRONG);
            giuongBenhRepo.save(giuong);
        }
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        LanNhapVien e = get(id);
        chamSocRepo.deleteAll(chamSocRepo.findByLanNhapVienIdOrderByIdAsc(e.getId()));
        // Tháo các lần khám khỏi lần nhập viện (giữ lại lần khám như khám thường)
        List<LanKhamBenh> cacLanKham = lanKhamRepo.findByLanNhapVienIdOrderByThoiGianKhamAsc(e.getId());
        for (LanKhamBenh k : cacLanKham) {
            k.setLanNhapVien(null);
        }
        lanKhamRepo.saveAll(cacLanKham);
        if (e.getTrangThai() == TrangThaiNhapVien.DANG_DIEU_TRI && e.getGiuongBenh() != null) {
            GiuongBenh giuong = e.getGiuongBenh();
            if (giuong.getTrangThai() == TrangThaiGiuong.DANG_SU_DUNG) {
                giuong.setTrangThai(TrangThaiGiuong.TRONG);
                giuongBenhRepo.save(giuong);
            }
        }
        repo.delete(e);
    }

    private void replaceYTa(LanNhapVien nhapVien, List<Long> yTaIds) {
        chamSocRepo.deleteAll(chamSocRepo.findByLanNhapVienIdOrderByIdAsc(nhapVien.getId()));
        if (yTaIds == null || yTaIds.isEmpty()) {
            return;
        }
        List<ChamSoc> rows = new ArrayList<>();
        for (Long yTaId : yTaIds.stream().distinct().toList()) {
            ChamSoc row = new ChamSoc();
            row.setYTa(yTaRepo.findById(yTaId)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy y tá " + yTaId)));
            row.setLanNhapVien(nhapVien);
            rows.add(row);
        }
        chamSocRepo.saveAll(rows);
    }

    private void apply(LanNhapVien e, LanNhapVienReq r) {
        e.setMaNhapVien(r.maNhapVien());
        e.setNgayNhap(r.ngayNhap());
        e.setNgayXuat(r.ngayXuat());
        e.setLyDoNhapVien(r.lyDoNhapVien());
        e.setGhiChu(r.ghiChu());
        if (r.trangThai() != null) {
            e.setTrangThai(r.trangThai());
        }
        e.setGiuongBenh(giuongBenhRepo.findById(r.giuongBenhId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy giường bệnh " + r.giuongBenhId())));
    }
}
