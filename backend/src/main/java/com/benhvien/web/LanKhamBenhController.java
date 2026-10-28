package com.benhvien.web;

import com.benhvien.domain.GiuongBenh;
import com.benhvien.domain.LanKhamBenh;
import com.benhvien.domain.LanNhapVien;
import com.benhvien.domain.TrangThaiGiuong;
import com.benhvien.domain.TrangThaiKham;
import com.benhvien.repository.BenhNhanRepository;
import com.benhvien.repository.GiuongBenhRepository;
import com.benhvien.repository.LanKhamBenhRepository;
import com.benhvien.repository.LanNhapVienRepository;
import com.benhvien.repository.LichLamViecRepository;
import com.benhvien.web.dto.LanKhamBenhReq;
import com.benhvien.web.dto.NhapVienTuLanKhamReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lan-kham-benh")
public class LanKhamBenhController {

    private final LanKhamBenhRepository repo;
    private final BenhNhanRepository benhNhanRepo;
    private final LichLamViecRepository lichLamViecRepo;
    private final LanNhapVienRepository lanNhapVienRepo;
    private final GiuongBenhRepository giuongBenhRepo;

    public LanKhamBenhController(LanKhamBenhRepository repo, BenhNhanRepository benhNhanRepo,
                                 LichLamViecRepository lichLamViecRepo, LanNhapVienRepository lanNhapVienRepo,
                                 GiuongBenhRepository giuongBenhRepo) {
        this.repo = repo;
        this.benhNhanRepo = benhNhanRepo;
        this.lichLamViecRepo = lichLamViecRepo;
        this.lanNhapVienRepo = lanNhapVienRepo;
        this.giuongBenhRepo = giuongBenhRepo;
    }

    @GetMapping
    public List<LanKhamBenh> list(@RequestParam(required = false) Long benhNhanId,
                                  @RequestParam(required = false) Long lichLamViecId,
                                  @RequestParam(required = false) Long lanNhapVienId,
                                  @RequestParam(required = false) TrangThaiKham trangThai) {
        if (benhNhanId != null) {
            return repo.findByBenhNhanIdOrderByThoiGianKhamDesc(benhNhanId);
        }
        if (lichLamViecId != null) {
            return repo.findByLichLamViecIdOrderByThoiGianKhamDesc(lichLamViecId);
        }
        if (lanNhapVienId != null) {
            return repo.findByLanNhapVienIdOrderByThoiGianKhamAsc(lanNhapVienId);
        }
        if (trangThai != null) {
            return repo.findByTrangThaiOrderByThoiGianKhamDesc(trangThai);
        }
        return repo.findAllByOrderByThoiGianKhamDesc();
    }

    @GetMapping("/{id}")
    public LanKhamBenh get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy lần khám " + id));
    }

    @PostMapping
    @Transactional
    public LanKhamBenh create(@Valid @RequestBody LanKhamBenhReq r) {
        LanKhamBenh e = new LanKhamBenh();
        apply(e, r);
        if (e.getThoiGianKham() == null) {
            e.setThoiGianKham(LocalDateTime.now());
        }
        e = repo.save(e);
        if (e.getMaLanKham() == null || e.getMaLanKham().isBlank()) {
            e.setMaLanKham("LK-" + String.format("%05d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public LanKhamBenh update(@PathVariable Long id, @Valid @RequestBody LanKhamBenhReq r) {
        LanKhamBenh e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        LanKhamBenh e = get(id);
        if (e.getLanNhapVien() != null) {
            throw new IllegalArgumentException("Lần khám này thuộc một lần nhập viện, hãy xử lý lần nhập viện trước khi xóa.");
        }
        repo.delete(e);
    }

    /**
     * Quan hệ "Nhập viên": từ một lần khám, tạo Lần Nhập Viện nằm trên một giường bệnh.
     * Giường phải đang trống; giường chuyển sang đang sử dụng và lần khám gắn với lần nhập viện.
     */
    @PostMapping("/{id}/nhap-vien")
    @Transactional
    public LanNhapVien nhapVien(@PathVariable Long id, @Valid @RequestBody NhapVienTuLanKhamReq r) {
        LanKhamBenh kham = get(id);
        if (kham.getLanNhapVien() != null) {
            throw new IllegalArgumentException("Lần khám này đã được nhập viện (mã " + kham.getLanNhapVien().getMaNhapVien() + ").");
        }
        GiuongBenh giuong = giuongBenhRepo.findById(r.giuongBenhId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy giường bệnh " + r.giuongBenhId()));
        if (giuong.getTrangThai() != TrangThaiGiuong.TRONG) {
            throw new IllegalArgumentException("Giường " + giuong.getMaGiuong() + " hiện không trống, hãy chọn giường khác.");
        }
        LanNhapVien nhapVien = new LanNhapVien();
        nhapVien.setNgayNhap(LocalDateTime.now());
        nhapVien.setLyDoNhapVien(r.lyDoNhapVien());
        nhapVien.setGhiChu(r.ghiChu());
        nhapVien.setGiuongBenh(giuong);
        nhapVien = lanNhapVienRepo.save(nhapVien);
        nhapVien.setMaNhapVien("NV-" + String.format("%05d", nhapVien.getId()));
        nhapVien = lanNhapVienRepo.save(nhapVien);

        giuong.setTrangThai(TrangThaiGiuong.DANG_SU_DUNG);
        giuongBenhRepo.save(giuong);

        kham.setLanNhapVien(nhapVien);
        repo.save(kham);
        return nhapVien;
    }

    private void apply(LanKhamBenh e, LanKhamBenhReq r) {
        e.setMaLanKham(r.maLanKham());
        e.setThoiGianKham(r.thoiGianKham());
        e.setTrieuChung(r.trieuChung());
        e.setChanDoan(r.chanDoan());
        if (r.trangThai() != null) {
            e.setTrangThai(r.trangThai());
        }
        e.setBenhNhan(benhNhanRepo.findById(r.benhNhanId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bệnh nhân " + r.benhNhanId())));
        e.setLichLamViec(r.lichLamViecId() == null ? null : lichLamViecRepo.findById(r.lichLamViecId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lịch làm việc " + r.lichLamViecId())));
        e.setLanNhapVien(r.lanNhapVienId() == null ? null : lanNhapVienRepo.findById(r.lanNhapVienId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lần nhập viện " + r.lanNhapVienId())));
    }
}
