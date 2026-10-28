package com.benhvien.web;

import com.benhvien.domain.BacSi;
import com.benhvien.repository.BacSiRepository;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.web.dto.BacSiReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/bac-si")
public class BacSiController {

    private final BacSiRepository repo;
    private final KhoaRepository khoaRepo;

    public BacSiController(BacSiRepository repo, KhoaRepository khoaRepo) {
        this.repo = repo;
        this.khoaRepo = khoaRepo;
    }

    @GetMapping
    public List<BacSi> list(@RequestParam(required = false) Long khoaId,
                            @RequestParam(required = false) String q) {
        if (khoaId != null) {
            return repo.findByKhoaIdOrderById(khoaId);
        }
        if (q != null && !q.isBlank()) {
            return repo.findByHoTenContainingIgnoreCaseOrMaBacSiContainingIgnoreCaseOrderById(q, q);
        }
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public BacSi get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy bác sĩ " + id));
    }

    @PostMapping
    @Transactional
    public BacSi create(@Valid @RequestBody BacSiReq r) {
        BacSi e = new BacSi();
        apply(e, r);
        e = repo.save(e);
        if (e.getMaBacSi() == null || e.getMaBacSi().isBlank()) {
            e.setMaBacSi("BS-" + String.format("%04d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public BacSi update(@PathVariable Long id, @Valid @RequestBody BacSiReq r) {
        BacSi e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(BacSi e, BacSiReq r) {
        e.setMaBacSi(r.maBacSi());
        e.setHoTen(r.hoTen());
        e.setChuyenMon(r.chuyenMon());
        e.setHocHam(r.hocHam());
        e.setSoDienThoai(r.soDienThoai());
        e.setEmail(r.email());
        e.setKhoa(khoaRepo.findById(r.khoaId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy khoa " + r.khoaId())));
    }
}
