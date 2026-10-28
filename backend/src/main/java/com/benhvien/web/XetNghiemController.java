package com.benhvien.web;

import com.benhvien.domain.XetNghiem;
import com.benhvien.repository.LanKhamBenhRepository;
import com.benhvien.repository.LichLamViecRepository;
import com.benhvien.repository.XetNghiemRepository;
import com.benhvien.web.dto.XetNghiemReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/xet-nghiem")
public class XetNghiemController {

    private final XetNghiemRepository repo;
    private final LanKhamBenhRepository lanKhamRepo;
    private final LichLamViecRepository lichLamViecRepo;

    public XetNghiemController(XetNghiemRepository repo, LanKhamBenhRepository lanKhamRepo,
                               LichLamViecRepository lichLamViecRepo) {
        this.repo = repo;
        this.lanKhamRepo = lanKhamRepo;
        this.lichLamViecRepo = lichLamViecRepo;
    }

    @GetMapping
    public List<XetNghiem> list(@RequestParam(required = false) Long lanKhamBenhId,
                                @RequestParam(required = false) Long lichLamViecId) {
        if (lanKhamBenhId != null) {
            return repo.findByLanKhamBenhIdOrderByIdDesc(lanKhamBenhId);
        }
        if (lichLamViecId != null) {
            return repo.findByLichLamViecIdOrderByIdDesc(lichLamViecId);
        }
        return repo.findAllByOrderByIdDesc();
    }

    @GetMapping("/{id}")
    public XetNghiem get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy xét nghiệm " + id));
    }

    @PostMapping
    @Transactional
    public XetNghiem create(@Valid @RequestBody XetNghiemReq r) {
        XetNghiem e = new XetNghiem();
        apply(e, r);
        if (e.getThoiGian() == null) {
            e.setThoiGian(LocalDateTime.now());
        }
        e = repo.save(e);
        if (e.getMaXetNghiem() == null || e.getMaXetNghiem().isBlank()) {
            e.setMaXetNghiem("XN-" + String.format("%05d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public XetNghiem update(@PathVariable Long id, @Valid @RequestBody XetNghiemReq r) {
        XetNghiem e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(XetNghiem e, XetNghiemReq r) {
        e.setMaXetNghiem(r.maXetNghiem());
        e.setTenXetNghiem(r.tenXetNghiem());
        e.setLoaiXetNghiem(r.loaiXetNghiem());
        e.setThoiGian(r.thoiGian());
        e.setKetQua(r.ketQua());
        if (r.trangThai() != null) {
            e.setTrangThai(r.trangThai());
        }
        e.setLanKhamBenh(lanKhamRepo.findById(r.lanKhamBenhId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lần khám " + r.lanKhamBenhId())));
        e.setLichLamViec(r.lichLamViecId() == null ? null : lichLamViecRepo.findById(r.lichLamViecId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lịch làm việc " + r.lichLamViecId())));
    }
}
