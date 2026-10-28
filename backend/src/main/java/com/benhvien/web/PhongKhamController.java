package com.benhvien.web;

import com.benhvien.domain.PhongKham;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.repository.PhongKhamRepository;
import com.benhvien.web.dto.PhongKhamReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/phong-kham")
public class PhongKhamController {

    private final PhongKhamRepository repo;
    private final KhoaRepository khoaRepo;

    public PhongKhamController(PhongKhamRepository repo, KhoaRepository khoaRepo) {
        this.repo = repo;
        this.khoaRepo = khoaRepo;
    }

    @GetMapping
    public List<PhongKham> list(@RequestParam(required = false) Long khoaId) {
        return khoaId == null ? repo.findAll() : repo.findByKhoaIdOrderById(khoaId);
    }

    @GetMapping("/{id}")
    public PhongKham get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng khám " + id));
    }

    @PostMapping
    @Transactional
    public PhongKham create(@Valid @RequestBody PhongKhamReq r) {
        PhongKham e = new PhongKham();
        apply(e, r);
        e = repo.save(e);
        if (e.getMaPhong() == null || e.getMaPhong().isBlank()) {
            e.setMaPhong("PK-" + String.format("%04d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public PhongKham update(@PathVariable Long id, @Valid @RequestBody PhongKhamReq r) {
        PhongKham e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(PhongKham e, PhongKhamReq r) {
        e.setMaPhong(r.maPhong());
        e.setTenPhong(r.tenPhong());
        e.setKhoa(khoaRepo.findById(r.khoaId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy khoa " + r.khoaId())));
    }
}
