package com.benhvien.web;

import com.benhvien.domain.GiuongBenh;
import com.benhvien.domain.TrangThaiGiuong;
import com.benhvien.repository.GiuongBenhRepository;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.web.dto.GiuongBenhReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/giuong-benh")
public class GiuongBenhController {

    private final GiuongBenhRepository repo;
    private final KhoaRepository khoaRepo;

    public GiuongBenhController(GiuongBenhRepository repo, KhoaRepository khoaRepo) {
        this.repo = repo;
        this.khoaRepo = khoaRepo;
    }

    @GetMapping
    public List<GiuongBenh> list(@RequestParam(required = false) Long khoaId,
                                 @RequestParam(required = false) TrangThaiGiuong trangThai) {
        if (khoaId != null && trangThai != null) {
            return repo.findByKhoaIdAndTrangThaiOrderById(khoaId, trangThai);
        }
        if (khoaId != null) {
            return repo.findByKhoaIdOrderById(khoaId);
        }
        if (trangThai != null) {
            return repo.findByTrangThaiOrderById(trangThai);
        }
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public GiuongBenh get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy giường bệnh " + id));
    }

    @PostMapping
    @Transactional
    public GiuongBenh create(@Valid @RequestBody GiuongBenhReq r) {
        GiuongBenh e = new GiuongBenh();
        apply(e, r);
        e = repo.save(e);
        if (e.getMaGiuong() == null || e.getMaGiuong().isBlank()) {
            e.setMaGiuong("G-" + String.format("%04d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public GiuongBenh update(@PathVariable Long id, @Valid @RequestBody GiuongBenhReq r) {
        GiuongBenh e = get(id);
        if (e.getTrangThai() == TrangThaiGiuong.DANG_SU_DUNG
                && r.trangThai() != TrangThaiGiuong.DANG_SU_DUNG) {
            throw new IllegalArgumentException("Giường đang có bệnh nhân nằm, không thể đổi trạng thái. Hãy xuất viện bệnh nhân trước.");
        }
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        GiuongBenh e = get(id);
        if (e.getTrangThai() == TrangThaiGiuong.DANG_SU_DUNG) {
            throw new IllegalArgumentException("Giường đang được sử dụng, không thể xóa.");
        }
        repo.delete(e);
    }

    private void apply(GiuongBenh e, GiuongBenhReq r) {
        e.setMaGiuong(r.maGiuong());
        e.setLoaiGiuong(r.loaiGiuong());
        if (r.trangThai() != null) {
            e.setTrangThai(r.trangThai());
        }
        e.setKhoa(khoaRepo.findById(r.khoaId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy khoa " + r.khoaId())));
    }
}
