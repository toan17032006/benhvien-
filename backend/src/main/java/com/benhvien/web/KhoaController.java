package com.benhvien.web;

import com.benhvien.domain.Khoa;
import com.benhvien.repository.BenhVienRepository;
import com.benhvien.repository.KhoaRepository;
import com.benhvien.web.dto.KhoaReq;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/khoa")
public class KhoaController {

    private final KhoaRepository repo;
    private final BenhVienRepository benhVienRepo;

    public KhoaController(KhoaRepository repo, BenhVienRepository benhVienRepo) {
        this.repo = repo;
        this.benhVienRepo = benhVienRepo;
    }

    @GetMapping
    public List<Khoa> list(@RequestParam(required = false) Long benhVienId) {
        return benhVienId == null ? repo.findAll() : repo.findByBenhVienIdOrderById(benhVienId);
    }

    @GetMapping("/{id}")
    public Khoa get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy khoa " + id));
    }

    @PostMapping
    @Transactional
    public Khoa create(@Valid @RequestBody KhoaReq r) {
        Khoa e = new Khoa();
        apply(e, r);
        e = repo.save(e);
        if (e.getMaKhoa() == null || e.getMaKhoa().isBlank()) {
            e.setMaKhoa("KHOA-" + String.format("%04d", e.getId()));
        }
        return repo.save(e);
    }

    @PutMapping("/{id}")
    @Transactional
    public Khoa update(@PathVariable Long id, @Valid @RequestBody KhoaReq r) {
        Khoa e = get(id);
        apply(e, r);
        return repo.save(e);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        repo.delete(get(id));
    }

    private void apply(Khoa e, KhoaReq r) {
        e.setMaKhoa(r.maKhoa());
        e.setTenKhoa(r.tenKhoa());
        e.setMoTa(r.moTa());
        e.setBenhVien(benhVienRepo.findById(r.benhVienId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bệnh viện " + r.benhVienId())));
    }
}
