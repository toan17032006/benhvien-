package com.benhvien.web;

import com.benhvien.domain.DonThuoc;
import com.benhvien.repository.DonThuocRepository;
import com.benhvien.repository.LanKhamBenhRepository;
import com.benhvien.web.dto.DonThuocReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/don-thuoc")
public class DonThuocController {

    private final DonThuocRepository repo;
    private final LanKhamBenhRepository lanKhamRepo;

    public DonThuocController(DonThuocRepository repo, LanKhamBenhRepository lanKhamRepo) {
        this.repo = repo;
        this.lanKhamRepo = lanKhamRepo;
    }

    @GetMapping
    public List<DonThuoc> list(@RequestParam(required = false) Long lanKhamBenhId) {
        return lanKhamBenhId == null ? repo.findAll() : repo.findByLanKhamBenhIdOrderByNgayKeDesc(lanKhamBenhId);
    }

    @GetMapping("/{id}")
    public DonThuoc get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn thuốc " + id));
    }

    @PostMapping
    @Transactional
    public DonThuoc create(@Valid @RequestBody DonThuocReq r) {
        DonThuoc e = new DonThuoc();
        apply(e, r);
        if (e.getNgayKe() == null) {
            e.setNgayKe(LocalDateTime.now());
        }
        e = repo.save(e);
        if (e.getMaDonThuoc() == null || e.getMaDonThuoc().isBlank()) {
            e.setMaDonThuoc("DT-" + String.format("%05d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public DonThuoc update(@PathVariable Long id, @Valid @RequestBody DonThuocReq r) {
        DonThuoc e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(DonThuoc e, DonThuocReq r) {
        e.setMaDonThuoc(r.maDonThuoc());
        e.setNgayKe(r.ngayKe());
        e.setGhiChu(r.ghiChu());
        e.setLanKhamBenh(lanKhamRepo.findById(r.lanKhamBenhId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lần khám " + r.lanKhamBenhId())));
    }
}
